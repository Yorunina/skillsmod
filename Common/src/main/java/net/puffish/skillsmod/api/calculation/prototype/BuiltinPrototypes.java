package net.puffish.skillsmod.api.calculation.prototype;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;

import java.util.Optional;

public final class BuiltinPrototypes {
	private BuiltinPrototypes() { }

	public static final Prototype<Double> NUMBER = Prototype.create(SkillsMod.createIdentifier("number"));
	public static final Prototype<Boolean> BOOLEAN = Prototype.create(SkillsMod.createIdentifier("boolean"));
	public static final Prototype<MinecraftServer> SERVER = Prototype.create(new Identifier("server"));
	public static final Prototype<ServerWorld> WORLD = Prototype.create(new Identifier("world"));
	public static final Prototype<EntityType<?>> ENTITY_TYPE = Prototype.create(new Identifier("entity_type"));
	public static final Prototype<Entity> ENTITY = Prototype.create(new Identifier("entity"));
	public static final Prototype<LivingEntity> LIVING_ENTITY = Prototype.create(new Identifier("living_entity"));
	public static final Prototype<ServerPlayerEntity> PLAYER = Prototype.create(new Identifier("player"));
	public static final Prototype<Item> ITEM = Prototype.create(new Identifier("item"));
	public static final Prototype<ItemStack> ITEM_STACK = Prototype.create(new Identifier("item_stack"));
	public static final Prototype<Block> BLOCK = Prototype.create(new Identifier("block"));
	public static final Prototype<BlockState> BLOCK_STATE = Prototype.create(new Identifier("block_state"));
	public static final Prototype<DamageType> DAMAGE_TYPE = Prototype.create(new Identifier("damage_type"));
	public static final Prototype<DamageSource> DAMAGE_SOURCE = Prototype.create(new Identifier("damage_source"));
	public static final Prototype<StatType<?>> STAT_TYPE = Prototype.create(new Identifier("stat_type"));
	public static final Prototype<Stat<?>> STAT = Prototype.create(new Identifier("stat"));
	public static final Prototype<StatusEffectInstance> STATUS_EFFECT_INSTANCE = Prototype.create(new Identifier("status_effect_instance"));
	public static final Prototype<EntityAttributeInstance> ENTITY_ATTRIBUTE_INSTANCE = Prototype.create(new Identifier("entity_attribute_instance"));

	static {
		WORLD.registerOperation(
				new Identifier("server"),
				SERVER,
				OperationFactory.create(ServerWorld::getServer)
		);
		WORLD.registerOperation(
				new Identifier("time_of_day"),
				NUMBER,
				OperationFactory.create(world -> (double) world.getTimeOfDay())
		);

		ENTITY.registerOperation(
				new Identifier("type"),
				ENTITY_TYPE,
				OperationFactory.create(Entity::getType)
		);
		ENTITY.registerOperation(
				new Identifier("world"),
				WORLD,
				OperationFactory.create(entity -> (ServerWorld) entity.getWorld())
		);

		LIVING_ENTITY.registerOperation(
				new Identifier("entity"),
				ENTITY,
				OperationFactory.create(p -> p)
		);
		LIVING_ENTITY.registerOperation(
				new Identifier("world"),
				WORLD,
				OperationFactory.create(livingEntity -> (ServerWorld) livingEntity.getWorld())
		);
		LIVING_ENTITY.registerOperation(
				new Identifier("type"),
				ENTITY_TYPE,
				OperationFactory.create(Entity::getType)
		);
		LIVING_ENTITY.registerOperation(
				new Identifier("max_health"),
				NUMBER,
				OperationFactory.create(livingEntity -> (double) livingEntity.getMaxHealth())
		);
		LIVING_ENTITY.registerOperation(
				new Identifier("health"),
				NUMBER,
				OperationFactory.create(livingEntity -> (double) livingEntity.getHealth())
		);

		PLAYER.registerOperation(
				new Identifier("living_entity"),
				LIVING_ENTITY,
				OperationFactory.create(p -> p)
		);
		PLAYER.registerOperation(
				new Identifier("entity"),
				ENTITY,
				OperationFactory.create(p -> p)
		);
		PLAYER.registerOperation(
				new Identifier("world"),
				WORLD,
				OperationFactory.create(player -> (ServerWorld) player.getWorld())
		);

		ITEM.registerOperation(
				new Identifier("saturation_modifier"),
				NUMBER,
				OperationFactory.create(item -> {
					var fc = item.getFoodComponent();
					return fc == null ? 0.0 : fc.getSaturationModifier();
				})
		);
		ITEM.registerOperation(
				new Identifier("nutrition"),
				NUMBER,
				OperationFactory.create(item -> {
					var fc = item.getFoodComponent();
					return fc == null ? 0.0 : fc.getHunger();
				})
		);

		ITEM_STACK.registerOperation(
				new Identifier("item"),
				ITEM,
				OperationFactory.create(ItemStack::getItem)
		);
		ITEM_STACK.registerOperation(
				new Identifier("count"),
				NUMBER,
				OperationFactory.create(itemStack -> (double) itemStack.getCount())
		);

		BLOCK.registerOperation(
				new Identifier("hardness"),
				NUMBER,
				OperationFactory.create(block -> (double) block.getHardness())
		);
		BLOCK.registerOperation(
				new Identifier("blast_resistance"),
				NUMBER,
				OperationFactory.create(block -> (double) block.getBlastResistance())
		);

		BLOCK_STATE.registerOperation(
				new Identifier("block"),
				BLOCK,
				OperationFactory.create(BlockState::getBlock)
		);

		DAMAGE_SOURCE.registerOperation(
				new Identifier("type"),
				DAMAGE_TYPE,
				OperationFactory.create(DamageSource::getType)
		);
		DAMAGE_SOURCE.registerOperation(
				new Identifier("attacker"),
				ENTITY,
				OperationFactory.createOptional(damageSource -> Optional.ofNullable(damageSource.getAttacker()))
		);
		DAMAGE_SOURCE.registerOperation(
				new Identifier("source"),
				ENTITY,
				OperationFactory.createOptional(damageSource -> Optional.ofNullable(damageSource.getSource()))
		);

		STAT.registerOperation(
				new Identifier("type"),
				STAT_TYPE,
				OperationFactory.create(Stat::getType)
		);

		STATUS_EFFECT_INSTANCE.registerOperation(
				new Identifier("level"),
				NUMBER,
				OperationFactory.create(effect -> (double) (effect.getAmplifier() + 1))
		);
		STATUS_EFFECT_INSTANCE.registerOperation(
				new Identifier("duration"),
				NUMBER,
				OperationFactory.create(effect -> (double) effect.getDuration())
		);

		ENTITY_ATTRIBUTE_INSTANCE.registerOperation(
				new Identifier("value"),
				NUMBER,
				OperationFactory.create(EntityAttributeInstance::getValue)
		);
		ENTITY_ATTRIBUTE_INSTANCE.registerOperation(
				new Identifier("base_value"),
				NUMBER,
				OperationFactory.create(EntityAttributeInstance::getBaseValue)
		);
	}
}
