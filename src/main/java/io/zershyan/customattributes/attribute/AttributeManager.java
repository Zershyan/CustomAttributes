package io.zershyan.customattributes.attribute;

import io.zershyan.customattributes.registry.config.ServerConfig;
import io.zershyan.customattributes.registry.packet.AttributeListPacket;
import io.zershyan.customattributes.registry.packet.AttributeUpdatePacket;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 服务端权威逻辑：谁在看界面、他看到什么值、他的修改请求怎么落到属性上。 */
public final class AttributeManager {
    private static final double EPSILON = 1.0e-6;
    /** 打开界面的最小间隔（刻）：只挡重复重建整表，不能长到让"关掉立刻再开"失灵 */
    private static final int OPEN_COOLDOWN_TICKS = 2;
    /** 同一 tick 内每玩家最多受理的提交数：要容得下关闭界面时一次补交多行键盘调整 */
    private static final int MAX_MODIFY_PER_TICK = 32;
    private static final Map<UUID, Watch> WATCHES = new HashMap<>();
    /** 玩家拥有的属性以及它们的默认基础值，只构建一次：属性注册与默认值在加载完成后不再变化 */
    private static List<Target> targets;

    private AttributeManager() {
    }

    /** 打开界面：订阅该玩家并下发完整列表。 */
    public static void open(ServerPlayer player) {
        int now = tickOf(player);
        Watch existing = WATCHES.get(player.getUUID());
        if (existing != null && now - existing.lastOpenTick < OPEN_COOLDOWN_TICKS) {
            return;
        }
        RangeRules.reload();
        List<AttributeEntry> entries = buildEntries(player);
        Watch watch = new Watch(now);
        for (AttributeEntry entry : entries) {
            watch.entries.put(entry.id(), entry);
        }
        WATCHES.put(player.getUUID(), watch);
        PacketDistributor.sendToPlayer(player, new AttributeListPacket(entries));
    }

    public static void close(ServerPlayer player) {
        WATCHES.remove(player.getUUID());
    }

    /**
     * 每个检查周期：把所有在线玩家的属性夹回区间，再把订阅玩家的数值与区间变化推给本人。
     * 关闭模组功能时清空订阅并通知界面，避免客户端停留在过期数据上。
     */
    public static void tick(MinecraftServer server) {
        PlayerList playerList = server.getPlayerList();
        // 只按 UUID 判活：死亡重生会换掉 ServerPlayer 实例，持有实例引用会变旧
        WATCHES.keySet().removeIf(uuid -> playerList.getPlayer(uuid) == null);
        if (!ServerConfig.isEnabled()) {
            if (!WATCHES.isEmpty()) {
                for (ServerPlayer player : playerList.getPlayers()) {
                    if (WATCHES.containsKey(player.getUUID())) {
                        PacketDistributor.sendToPlayer(player, new AttributeListPacket(List.of()));
                    }
                }
                WATCHES.clear();
            }
            return;
        }
        if (server.getTickCount() % ServerConfig.syncInterval() != 0) {
            return;
        }
        RangeRules.reload();
        for (ServerPlayer player : playerList.getPlayers()) {
            Watch watch = WATCHES.get(player.getUUID());
            enforcePlayer(player, watch);
            if (watch != null && watch.resync) {
                watch.resync = false;
                resync(player, watch);
            }
        }
    }

    /** 结构变了就整表重发：客户端据此增删行，比逐条增量简单且不会残留。 */
    private static void resync(ServerPlayer player, Watch watch) {
        watch.entries.clear();
        List<AttributeEntry> entries = buildEntries(player);
        for (AttributeEntry entry : entries) {
            watch.entries.put(entry.id(), entry);
        }
        PacketDistributor.sendToPlayer(player, new AttributeListPacket(entries));
    }

    /**
     * 单次遍历完成两件事：把超出区间的最终值夹回区间；给打开了界面的玩家推送变化。
     * 未订阅的玩家只做夹取，不产生任何网络流量。列表结构变化（属性出现或区间失效）时打标记，稍后整表重发。
     */
    private static void enforcePlayer(ServerPlayer player, Watch watch) {
        for (Target target : targets()) {
            AttributeInstance instance = player.getAttribute(target.holder);
            if (instance == null) {
                forget(watch, target.id);
                continue;
            }
            ValueRange range = RangeRules.rangeOf(instance, target.id, target.defaultValue).orElse(null);
            if (range == null) {
                // 区间失效（下界大于上界、被拉黑、配置无法满足）：界面上的这一行应当消失
                forget(watch, target.id);
                continue;
            }
            double value = instance.getValue();
            if (value < range.min() || value > range.max()) {
                value = clampInto(instance, target, range, value);
                if (Double.isNaN(value)) {
                    forget(watch, target.id);
                    continue;
                }
                range = RangeRules.rangeOf(instance, target.id, target.defaultValue).orElse(null);
                if (range == null) {
                    forget(watch, target.id);
                    continue;
                }
            }
            if (watch == null) {
                continue;
            }
            AttributeEntry candidate = entryOf(target.id, target.defaultValue, instance, value, range);
            AttributeEntry last = watch.entries.get(target.id);
            if (last == null) {
                // 新出现的可调属性：客户端还不知道这一行，需要整表重发
                watch.resync = true;
                continue;
            }
            if (!differs(last, candidate)) {
                continue;
            }
            watch.entries.put(target.id, candidate);
            PacketDistributor.sendToPlayer(player, new AttributeUpdatePacket(candidate));
        }
    }

    private static AttributeEntry entryOf(ResourceLocation id, double defaultValue, AttributeInstance instance,
                                          double value, ValueRange range) {
        boolean modified = Math.abs(instance.getBaseValue() - defaultValue) > EPSILON;
        return new AttributeEntry(id, value, range.min(), range.max(), modified);
    }

    /** 数值、区间或"是否已被改动"任一变化都要下发，否则界面会停在旧状态。 */
    private static boolean differs(AttributeEntry last, AttributeEntry current) {
        return Math.abs(current.value() - last.value()) > EPSILON
                || last.min() != current.min()
                || last.max() != current.max()
                || last.modified() != current.modified();
    }

    /**
     * 把越界值夹回区间，并确认夹完之后真的在区间里。
     * 夹不进说明配置自相矛盾（例如上界跟着当前值一起降）或数值被修饰锁死，此时回滚并停用该属性。
     *
     * @return 夹取后的最终值；无法满足时返回 {@link Double#NaN}
     */
    private static double clampInto(AttributeInstance instance, Target target, ValueRange range, double value) {
        double baseBefore = instance.getBaseValue();
        setFinalValue(instance, range.clamp(value));
        double clamped = instance.getValue();
        ValueRange after = RangeRules.rangeOf(instance, target.id, target.defaultValue).orElse(null);
        if (after == null || clamped < after.min() || clamped > after.max()) {
            instance.setBaseValue(baseBefore);
            RangeRules.markUnsatisfiable(target.id);
            return Double.NaN;
        }
        return clamped;
    }

    /** 属性从界面上消失时同步掉订阅快照里的条目，并请求整表重发。 */
    private static void forget(Watch watch, ResourceLocation id) {
        if (watch != null && watch.entries.remove(id) != null) {
            watch.resync = true;
        }
    }


    /**
     * 处理玩家提交的修改或重置，只有打开过界面的玩家才会被受理。
     * <p>区间方向不成立（下界大于上界）时连重置一并拒绝；区间塌缩成一个点时只允许重置，
     * 这样把值调到下限也不会把自己锁死。</p>
     */
    public static void apply(ServerPlayer player, ResourceLocation id, boolean reset, double requested) {
        if (!ServerConfig.isEnabled()) {
            return;
        }
        Watch watch = WATCHES.get(player.getUUID());
        if (watch == null || !watch.acceptModification(tickOf(player))) {
            return;
        }
        Holder.Reference<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);
        if (holder == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(holder);
        if (instance == null) {
            return;
        }
        RangeRules.reload();
        double defaultValue = defaultValueOf(holder);
        ValueRange range = RangeRules.rangeOf(instance, id, defaultValue).orElse(null);
        if (range != null) {
            if (reset) {
                // 直接写回默认基础值：区间会随后按新值重新求值，超出部分由周期夹取兜底
                instance.setBaseValue(defaultValue);
            } else if (range.isAscending() && Double.isFinite(requested)) {
                setFinalValue(instance, range.clamp(requested));
            }
        }
        // 无论是否生效都回报真实状态，避免界面停在服务端没有接受的值上
        syncNow(player, watch, id, instance, defaultValue);
    }

    private static void syncNow(ServerPlayer player, Watch watch, ResourceLocation id, AttributeInstance instance, double defaultValue) {
        if (!watch.entries.containsKey(id)) {
            return;
        }
        ValueRange range = RangeRules.rangeOf(instance, id, defaultValue).orElse(null);
        if (range == null) {
            return;
        }
        AttributeEntry entry = entryOf(id, defaultValue, instance, instance.getValue(), range);
        watch.entries.put(id, entry);
        PacketDistributor.sendToPlayer(player, new AttributeUpdatePacket(entry));
    }

    /**
     * 把最终值调到 {@code target}：最终值对基础值是仿射的，先用一次试值测斜率，再精确反解。
     */
    private static void setFinalValue(AttributeInstance instance, double target) {
        double before = instance.getValue();
        if (Math.abs(target - before) <= EPSILON) {
            return;
        }
        double baseBefore = instance.getBaseValue();
        double guess = baseBefore + (target - before);
        instance.setBaseValue(guess);
        double after = instance.getValue();
        if (Math.abs(after - target) <= EPSILON) {
            return;
        }
        double slope = (after - before) / (guess - baseBefore);
        if (!Double.isFinite(slope) || Math.abs(slope) <= EPSILON) {
            instance.setBaseValue(baseBefore);
            return;
        }
        instance.setBaseValue(baseBefore + (target - before) / slope);
    }

    private static List<AttributeEntry> buildEntries(ServerPlayer player) {
        if (!ServerConfig.isEnabled()) {
            return List.of();
        }
        List<AttributeEntry> entries = new ArrayList<>();
        for (Target target : targets()) {
            AttributeInstance instance = player.getAttribute(target.holder);
            if (instance == null) {
                continue;
            }
            ValueRange range = RangeRules.rangeOf(instance, target.id, target.defaultValue).orElse(null);
            if (range != null) {
                entries.add(entryOf(target.id, target.defaultValue, instance, instance.getValue(), range));
            }
        }
        return entries;
    }

    private static List<Target> targets() {
        List<Target> cached = targets;
        if (cached == null) {
            AttributeSupplier supplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
            List<Target> list = new ArrayList<>();
            for (Holder.Reference<Attribute> holder : BuiltInRegistries.ATTRIBUTE.holders().toList()) {
                ResourceLocation id = holder.unwrapKey().map(key -> key.location()).orElse(null);
                if (id == null || !(holder.value() instanceof RangedAttribute)) {
                    continue;
                }
                if (supplier != null && !supplier.hasAttribute(holder)) {
                    continue;
                }
                list.add(new Target(id, holder, defaultValueOf(supplier, holder)));
            }
            cached = targets = List.copyOf(list);
        }
        return cached;
    }

    private static double defaultValueOf(Holder<Attribute> holder) {
        return defaultValueOf(DefaultAttributes.getSupplier(EntityType.PLAYER), holder);
    }

    private static double defaultValueOf(AttributeSupplier supplier, Holder<Attribute> holder) {
        if (supplier != null && supplier.hasAttribute(holder)) {
            return supplier.getValue(holder);
        }
        return holder.value() instanceof RangedAttribute ranged ? ranged.getDefaultValue() : 0.0;
    }

    private static int tickOf(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return server == null ? 0 : server.getTickCount();
    }

    /** 一个玩家属性：ID、Holder 与默认基础值一次算好，逐 tick 只做字段访问。 */
    private record Target(ResourceLocation id, Holder.Reference<Attribute> holder, double defaultValue) {
    }

    /** 订阅快照：记录上次下发的完整条目，数值与区间任一变化都要重新推送。 */
    private static final class Watch {
        private final Map<ResourceLocation, AttributeEntry> entries = new HashMap<>();
        private final int lastOpenTick;
        private int budgetTick = -1;
        private int budget;
        private boolean resync;

        private Watch(int lastOpenTick) {
            this.lastOpenTick = lastOpenTick;
        }

        /** 同一 tick 内最多受理 {@link #MAX_MODIFY_PER_TICK} 次提交，避免客户端刷包。 */
        private boolean acceptModification(int tick) {
            if (budgetTick != tick) {
                budgetTick = tick;
                budget = 0;
            }
            return budget++ < MAX_MODIFY_PER_TICK;
        }
    }
}
