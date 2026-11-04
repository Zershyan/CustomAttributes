package io.zershyan.customattributes.attribute;

import io.zershyan.customattributes.registry.CustomAttributesAttachments;
import io.zershyan.customattributes.registry.CustomAttributesPackets;
import io.zershyan.customattributes.registry.config.ServerConfig;
import io.zershyan.customattributes.registry.packet.AttributeListPacket;
import io.zershyan.customattributes.registry.packet.AttributeUpdatePacket;
import io.zershyan.forgeattachment.attachment.IAttachmentHolder;
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
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
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
        Map<ResourceLocation, Double> targets = targetsOf(player);
        if (targets == null) {
            return;
        }
        int now = tickOf(player);
        Watch existing = WATCHES.get(player.getUUID());
        if (existing != null && now - existing.lastOpenTick < OPEN_COOLDOWN_TICKS) {
            return;
        }
        RangeRules.reload();
        List<AttributeEntry> entries = buildEntries(player, targets);
        Watch watch = new Watch(now);
        for (AttributeEntry entry : entries) {
            watch.entries.put(entry.id(), entry);
        }
        WATCHES.put(player.getUUID(), watch);
        sendTo(player, new AttributeListPacket(entries));
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
                        sendTo(player, new AttributeListPacket(List.of()));
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
        Map<ResourceLocation, Double> targets = targetsOf(player);
        if (targets == null) {
            return;
        }
        watch.entries.clear();
        List<AttributeEntry> entries = buildEntries(player, targets);
        for (AttributeEntry entry : entries) {
            watch.entries.put(entry.id(), entry);
        }
        sendTo(player, new AttributeListPacket(entries));
    }

    /**
     * 单次遍历完成三件事：清掉已不可用属性的调整、把最终值钉回玩家设定的目标（未设定的则夹进区间）、
     * 给打开了界面的玩家推送变化。未订阅的玩家只做前两件，不产生任何网络流量。
     */
    private static void enforcePlayer(ServerPlayer player, Watch watch) {
        Map<ResourceLocation, Double> targets = targetsOf(player);
        if (targets == null) {
            return;
        }
        dropUnavailable(player, targets);

        for (Target target : targets()) {
            AttributeInstance instance = player.getAttribute(target.attribute);
            if (instance == null) {
                forget(watch, target.id);
                continue;
            }
            ValueRange range = currentRange(instance, target.id, target.defaultValue);
            if (range == null) {
                // 区间暂时失效（下界大于上界）：保留玩家的目标，但这一行先不显示
                forget(watch, target.id);
                continue;
            }

            Double wanted = targets.get(target.id);
            double unadjusted = AttributeAdjustments.unadjustedValue(instance);
            double desired = range.clamp(wanted == null ? unadjusted : wanted);
            if (!syncValue(instance, desired, unadjusted)) {
                RangeRules.markUnsatisfiable(target.id);
                forget(watch, target.id);
                continue;
            }

            if (watch == null) {
                continue;
            }
            AttributeEntry candidate = new AttributeEntry(target.id, instance.getValue(),
                    range.min(), range.max(), wanted != null);
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
            sendTo(player, new AttributeUpdatePacket(candidate));
        }
    }

    /**
     * 让最终值落到 {@code desired}。
     * <p>目标值不依赖当前修饰，所以装备、药水或乘区变化后重新求解就能回到原来的数，不会跟着漂移。</p>
     */
    private static boolean syncValue(AttributeInstance instance, double desired, double unadjusted) {
        if (Math.abs(desired - unadjusted) <= EPSILON) {
            // 不需要偏移（未调整，或被夹回后正好等于原始值）：撤掉修饰器，别在装备变化后留下残留
            AttributeAdjustments.clear(instance);
            return true;
        }
        if (Math.abs(instance.getValue() - desired) <= EPSILON) {
            return true;
        }
        return AttributeAdjustments.setValue(instance, desired);
    }

    /** 名单变化后，把已经不再允许调整的属性上的目标与修饰器一起清掉，避免数值被无声地钉住。 */
    private static void dropUnavailable(ServerPlayer player, Map<ResourceLocation, Double> targets) {
        if (targets.isEmpty()) {
            return;
        }
        targets.keySet().removeIf(id -> {
            if (RangeRules.isAllowed(id)) {
                return false;
            }
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(id);
            AttributeInstance instance = attribute == null ? null : player.getAttribute(attribute);
            if (instance != null) {
                AttributeAdjustments.clear(instance);
            }
            return true;
        });
    }

    /** 数值、区间或"是否已被改动"任一变化都要下发，否则界面会停在旧状态。 */
    private static boolean differs(AttributeEntry last, AttributeEntry current) {
        return Math.abs(current.value() - last.value()) > EPSILON
                || last.min() != current.min()
                || last.max() != current.max()
                || last.modified() != current.modified();
    }

    /** 属性从界面上消失时同步掉订阅快照里的条目，并请求整表重发。 */
    private static void forget(Watch watch, ResourceLocation id) {
        if (watch != null && watch.entries.remove(id) != null) {
            watch.resync = true;
        }
    }

    /**
     * 处理玩家提交的修改或重置，只有打开过界面的玩家才会被受理。
     * <p>区间方向不成立（下界大于上界）时连重置一并拒绝；区间塌缩成一个点时只允许重置。</p>
     */
    public static void apply(ServerPlayer player, ResourceLocation id, boolean reset, double requested) {
        if (!ServerConfig.isEnabled()) {
            return;
        }
        Watch watch = WATCHES.get(player.getUUID());
        if (watch == null || !watch.acceptModification(tickOf(player))) {
            return;
        }
        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(id);
        if (attribute == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        Map<ResourceLocation, Double> targets = targetsOf(player);
        if (targets == null) {
            return;
        }
        RangeRules.reload();
        double defaultValue = defaultValueOf(attribute);
        ValueRange range = currentRange(instance, id, defaultValue);
        if (range != null) {
            if (reset) {
                targets.remove(id);
                AttributeAdjustments.clear(instance);
            } else if (range.isAscending() && Double.isFinite(requested)) {
                double target = range.clamp(requested);
                targets.put(id, target);
                AttributeAdjustments.setValue(instance, target);
            }
        }
        // 无论是否生效都回报真实状态，避免界面停在服务端没有接受的值上
        syncNow(player, watch, id, instance, defaultValue, targets);
    }

    private static void syncNow(ServerPlayer player, Watch watch, ResourceLocation id,
                                AttributeInstance instance, double defaultValue, Map<ResourceLocation, Double> targets) {
        if (!watch.entries.containsKey(id)) {
            return;
        }
        ValueRange range = currentRange(instance, id, defaultValue);
        if (range == null) {
            return;
        }
        AttributeEntry entry = new AttributeEntry(id, instance.getValue(), range.min(), range.max(), targets.containsKey(id));
        watch.entries.put(id, entry);
        sendTo(player, new AttributeUpdatePacket(entry));
    }

    private static List<AttributeEntry> buildEntries(ServerPlayer player, Map<ResourceLocation, Double> targets) {
        if (!ServerConfig.isEnabled()) {
            return List.of();
        }
        List<AttributeEntry> entries = new ArrayList<>();
        for (Target target : targets()) {
            AttributeInstance instance = player.getAttribute(target.attribute);
            if (instance == null) {
                continue;
            }
            ValueRange range = currentRange(instance, target.id, target.defaultValue);
            if (range != null) {
                entries.add(new AttributeEntry(target.id, instance.getValue(), range.min(), range.max(),
                        targets.containsKey(target.id)));
            }
        }
        return entries;
    }

    /** 求值区间时传的是"未经过本模组调整"的最终值，玩家自己调低不会让区间缩水。 */
    private static ValueRange currentRange(AttributeInstance instance, ResourceLocation id, double defaultValue) {
        return RangeRules.rangeOf(instance, id, defaultValue, AttributeAdjustments.unadjustedValue(instance)).orElse(null);
    }

    /** 玩家存档里的目标值；拿不到附件时返回 null，调用方直接放弃本次处理。 */
    @Nullable
    private static Map<ResourceLocation, Double> targetsOf(ServerPlayer player) {
        if (player instanceof IAttachmentHolder holder) {
            return holder.getData(CustomAttributesAttachments.TARGETS);
        }
        return null;
    }

    private static List<Target> targets() {
        List<Target> cached = targets;
        if (cached == null) {
            AttributeSupplier supplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
            List<Target> list = new ArrayList<>();
            for (ResourceLocation id : BuiltInRegistries.ATTRIBUTE.keySet()) {
                Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(id);
                if (!(attribute instanceof RangedAttribute)) {
                    continue;
                }
                if (supplier != null && !supplier.hasAttribute(attribute)) {
                    continue;
                }
                list.add(new Target(id, attribute, defaultValueOf(attribute)));
            }
            cached = targets = List.copyOf(list);
        }
        return cached;
    }

    private static double defaultValueOf(Attribute attribute) {
        AttributeSupplier supplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
        if (supplier != null && supplier.hasAttribute(attribute)) {
            return supplier.getValue(attribute);
        }
        return attribute instanceof RangedAttribute ranged ? ranged.getDefaultValue() : 0.0;
    }

    private static void sendTo(ServerPlayer player, Object packet) {
        CustomAttributesPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private static int tickOf(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return server == null ? 0 : server.getTickCount();
    }

    /** 一个玩家属性：ID、属性对象与默认基础值一次算好，逐 tick 只做字段访问。 */
    private record Target(ResourceLocation id, Attribute attribute, double defaultValue) {
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
