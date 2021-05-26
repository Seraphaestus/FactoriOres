package seraphaestus.factoriores.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.content.PonderPalette;
import com.simibubi.create.foundation.ponder.elements.EntityElement;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockMechanicalMiner;
import seraphaestus.factoriores.compat.CreateRegistrar;

public class PonderScenes {
	
	public static void mechanicalMiner(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_miner", "Extracting ores using the Mechanical Miner");
		
		scene.configureBasePlate(0, 0, 5);
		
		Selection stackedCogs = util.select.fromTo(3, 1, 2, 3, 2, 2);
		
		Selection belt = util.select.fromTo(1, 1, 5, 0, 1, 2).add(util.select.position(1, 2, 2));
		Selection beltCog = util.select.position(2, 0, 5);

		scene.world.showSection(util.select.layer(0).substract(beltCog), Direction.UP);

		BlockPos miner = util.grid.at(2, 2, 2);
		Selection minerSelect = util.select.position(2, 2, 2);
		
		ItemStack outputItem = new ItemStack(Items.DIAMOND);
		Emitter particleEmitter = oreMiningEmitter(util, Registrar.oreDeposits.stream().filter(o -> o.name == "diamond").findFirst().get().getDefaultState());

		//init kinetic speeds bc for some reason it doesn't want to do it automatically? is it part of the millstone nbt that the cogs are already moving?
		scene.world.setKineticSpeed(util.select.everywhere(), 16);
		scene.world.setKineticSpeed(minerSelect, 0);

		scene.idle(5);
		scene.world.showSection(util.select.position(4, 1, 3), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(miner), Direction.DOWN);
		scene.idle(10);
		Vector3d minerTop = util.vector.topOf(miner);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("The Mechanical Miner extracts ores by drilling when placed on an ore deposit")
			.pointAt(minerTop)
			.placeNearTarget();
		scene.idle(70);

		scene.world.showSection(stackedCogs, Direction.DOWN);
		scene.idle(10);
		scene.world.setKineticSpeed(minerSelect, 16);
		scene.world.setBlock(miner, CreateRegistrar.blockMechanicalMiner.get().getDefaultState().with(BlockMechanicalMiner.ENABLED, true), false);
		scene.effects.indicateSuccess(miner);
		scene.effects.emitParticles(minerTop.subtract(0, 2, 0), particleEmitter, ORE_PARTICLE_DENSITY, 360);
		scene.idle(10);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("It can be powered from the side using cogwheels")
			.pointAt(util.vector.topOf(miner.east()))
			.placeNearTarget();
		scene.idle(70);
		
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> 4 * f);
		scene.effects.rotationSpeedIndicator(util.grid.at(5, 0, 3));
		scene.overlay.showText(50)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(miner, Direction.WEST))
			.text("Its mining speed depends on the Rotational Input");
		scene.idle(45);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> f / 4);
		scene.effects.rotationSpeedIndicator(util.grid.at(5, 0, 3));
		scene.idle(15);
		
		scene.overlay.showText(50)
		.attachKeyFrame()
			.text("After some time, the result can be obtained via Right-click")
			.pointAt(util.vector.blockSurface(miner, Direction.WEST))
			.placeNearTarget();
		scene.idle(60);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(miner, Direction.NORTH), Pointing.RIGHT).rightClick()
				.withItem(outputItem),
			40);
		scene.idle(50);

		scene.addKeyframe();
		scene.world.showSection(beltCog, Direction.UP);
		scene.world.showSection(belt, Direction.EAST);
		scene.idle(15);
		
		BlockPos beltPos = util.grid.at(1, 1, 2);
		scene.world.createItemOnBelt(beltPos, Direction.EAST, outputItem);
		scene.idle(35);

		scene.overlay.showText(50)
			.text("The outputs can also be extracted by automation")
			.pointAt(util.vector.blockSurface(miner, Direction.WEST)
				.add(-.5, .4, 0))
			.placeNearTarget();
		scene.idle(60);
		
		scene.world.setBlock(miner.down(2), Registrar.blockGangue.getDefaultState(), true);
		scene.world.setBlock(miner, CreateRegistrar.blockMechanicalMiner.get().getDefaultState().with(BlockMechanicalMiner.ENABLED, false), false);
	}
	
	public static void mechanicalMinerLixiviant(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_miner_lixiviant", "Using the Mechanical Miner with lixiviants");
		scene.configureBasePlate(0, 0, 5);
		
		Selection originCogs = util.select.position(5, 0, 3).add(util.select.position(4, 1, 3));
		Selection stackedCogs = util.select.fromTo(3, 1, 2, 3, 2, 2);
		
		BlockPos miner = util.grid.at(2, 2, 2);
		Selection minerSelect = util.select.position(2, 2, 2);
		BlockPos tank = util.grid.at(2, 3, 2);
		Selection tankSelect = util.select.position(2, 3, 2);
		
		ItemStack outputItem = new ItemStack(AllItems.CRUSHED_URANIUM.get());
		Emitter particleEmitter = oreMiningEmitter(util, Registrar.oreDeposits.stream().filter(o -> o.name == "uranium").findFirst().get().getDefaultState());

		scene.world.showSection(util.select.layer(0).substract(originCogs), Direction.UP);
		
		scene.idle(5);
		Vector3d oreTop = util.vector.topOf(2, 0, 2);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Some ores are too hard to be mined by hand and need to be dissolved by a lixiviant")
			.pointAt(oreTop)
			.placeNearTarget();
		scene.idle(70);
		
		scene.world.showSection(originCogs, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(stackedCogs, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(miner), Direction.DOWN);
		scene.idle(15);
		
		scene.world.setKineticSpeed(util.select.everywhere(), 16);
		scene.idle(10);
		
		scene.overlay.showText(40)
			.colored(PonderPalette.RED)
			.text("x")
			.independent(60)
			.placeNearTarget();
		scene.idle(50);
		
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		
		scene.idle(20);
		scene.world.showSection(tankSelect, Direction.DOWN);
		scene.idle(10);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("One valid lixiviant, per default configuration, is Sulfuric Acid")
			.pointAt(util.vector.blockSurface(tank, Direction.WEST))
			.placeNearTarget();
		scene.idle(30);
		
		/*for (int i = 1; i <= 4; i++) {
			FluidStack fluid = new FluidStack(Registrar.sulfuricAcid.get(), 1000 * i);
			FactoriOres.LOGGER.debug(fluid.getDisplayName().toString() + ", " + fluid.getAmount());
			scene.world.modifyTileEntity(tank, FluidTankTileEntity.class, 
					te -> te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null).fill(fluid, FluidAction.EXECUTE));
			scene.idle(10);
		}*/
		//couldn't get actual fluid to work so we're using armor stand bootlegs
		Vector3d p = util.vector.centerOf(tank).subtract(0, 2.3, 0);
		ElementLink<EntityElement> fakeLiquid = scene.world.createEntity(w -> {
			ArmorStandEntity entity = new ArmorStandEntity(w, p.x, p.y, p.z);
			entity.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Registrar.sulfurBlock.asItem(), 1));
			entity.setNoGravity(true);
			entity.setInvisible(true);
			return entity;
		});
		
		for (int i = 1; i <= 25; i++) {
			final double height = p.y + 0.01 * i;
			scene.world.modifyEntity(fakeLiquid, e -> e.setPos(p.x, height, p.z));
			scene.idle(1);
		}
		
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.everywhere(), 16);
		scene.world.setBlock(miner, CreateRegistrar.blockMechanicalMiner.get().getDefaultState().with(BlockMechanicalMiner.ENABLED, true), false);
		scene.effects.emitParticles(util.vector.topOf(miner).subtract(0, 2, 0), particleEmitter, ORE_PARTICLE_DENSITY, 120);
		scene.effects.indicateSuccess(miner.down());
		scene.idle(50);
		
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(miner, Direction.NORTH), Pointing.RIGHT).rightClick()
				.withItem(outputItem),
			40);
		scene.idle(70);
		
		scene.world.setBlock(miner.down(2), Registrar.blockGangue.getDefaultState(), true);
		scene.world.setBlock(miner, CreateRegistrar.blockMechanicalMiner.get().getDefaultState().with(BlockMechanicalMiner.ENABLED, false), false);
	}
	
	public static void mechanicalMinerFluidDeposits(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_miner_fluid_deposits", "Extracting fluids using the Mechanical Miner");
		scene.configureBasePlate(0, 0, 5);
		
		Selection originCogs = util.select.position(5, 0, 3).add(util.select.position(4, 1, 3));
		Selection stackedCogs = util.select.fromTo(3, 1, 2, 3, 2, 2);
		
		BlockPos miner = util.grid.at(2, 2, 2);
		Selection minerSelect = util.select.position(2, 2, 2);
		BlockPos tank = util.grid.at(2, 3, 2);
		Selection tankSelect = util.select.position(2, 3, 2);
		
		Emitter particleEmitter = oreMiningEmitter(util, Registrar.fluidDeposits.stream().filter(o -> o.name == "water").findFirst().get().getDefaultState());

		scene.world.showSection(util.select.layer(0).substract(originCogs), Direction.UP);
		
		scene.idle(5);
		Vector3d oreTop = util.vector.topOf(2, 0, 2);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Some deposits contain fluids, and cannot be mined by hand")
			.pointAt(oreTop)
			.placeNearTarget();
		scene.idle(70);
		
		scene.world.showSection(originCogs, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(stackedCogs, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(miner), Direction.DOWN);
		scene.idle(15);
		
		scene.world.setKineticSpeed(util.select.everywhere(), 16);
		scene.idle(10);
		
		scene.overlay.showText(40)
			.colored(PonderPalette.RED)
			.text("x")
			.independent(60)
			.placeNearTarget();
		scene.idle(50);
		
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		
		scene.idle(20);
		scene.world.showSection(tankSelect, Direction.DOWN);
		scene.idle(10);
		scene.overlay.showText(120)
			.attachKeyFrame()
			.text("Simply place a valid tank on top of the miner")
			//.pointAt(util.vector.blockSurface(tank, Direction.WEST))
			.independent(40)
			.placeNearTarget();
		scene.idle(30);

		scene.world.setKineticSpeed(util.select.everywhere(), 16);
		scene.world.setBlock(miner, CreateRegistrar.blockMechanicalMiner.get().getDefaultState().with(BlockMechanicalMiner.ENABLED, true), false);
		scene.effects.emitParticles(util.vector.topOf(miner).subtract(0, 2, 0), particleEmitter, ORE_PARTICLE_DENSITY, 120);
		scene.effects.indicateSuccess(miner.down());
		scene.idle(10);
		
		Vector3d p = util.vector.centerOf(tank).subtract(0, 2.2, 0);
		ElementLink<EntityElement> fakeLiquid = scene.world.createEntity(w -> {
			ArmorStandEntity entity = new ArmorStandEntity(w, p.x, p.y, p.z);
			entity.setItemStackToSlot(EquipmentSlotType.HEAD, Items.BLUE_CONCRETE_POWDER.getDefaultInstance());
			entity.setNoGravity(true);
			entity.setInvisible(true);
			return entity;
		});
		
		for (int i = 1; i <= 5; i++) {
			final double height = p.y + 0.01 * i;
			scene.world.modifyEntity(fakeLiquid, e -> e.setPos(p.x, height, p.z));
			scene.idle(10);
			
			if (i == 2) {
				scene.overlay.showText(60)
				.text("and it will extract the fluid into the tank")
				//.pointAt(util.vector.blockSurface(tank, Direction.WEST))
				.independent(60)
				.placeNearTarget();
			}
		}
	
		scene.idle(30);
		
		scene.overlay.showControls(
				new InputWindowElement(util.vector.blockSurface(miner.up(), Direction.NORTH), Pointing.RIGHT).rightClick()
					.withItem(Items.BUCKET.getDefaultInstance()),
				10);
		scene.idle(10);
		scene.world.modifyEntity(fakeLiquid, e -> e.setPos(p.x, p.y + 0.03, p.z));
		scene.overlay.showControls(
				new InputWindowElement(util.vector.blockSurface(miner.up(), Direction.NORTH), Pointing.RIGHT).rightClick()
					.withItem(Items.WATER_BUCKET.getDefaultInstance()),
				30);
		scene.idle(60);
		
		scene.world.setBlock(miner.down(2), Registrar.blockGangue.getDefaultState(), true);
		scene.world.setBlock(miner, CreateRegistrar.blockMechanicalMiner.get().getDefaultState().with(BlockMechanicalMiner.ENABLED, false), false);
	}
	
	private static final float ORE_PARTICLE_DENSITY = 0.3f;
	private static Emitter oreMiningEmitter(SceneBuildingUtil util, BlockState ore) {
		BlockParticleData data = new BlockParticleData(ParticleTypes.BLOCK, ore);
		Vector3d motion = util.vector.of(0, 0.15, 0);
		return (w, x, y, z) -> w.addParticle(data, 
				Math.floor(x) + Create.random.nextFloat(),
				Math.floor(y) + 0.1, 
				Math.floor(z) + Create.random.nextFloat(), 
				motion.x, motion.y, motion.z);
	}
}
