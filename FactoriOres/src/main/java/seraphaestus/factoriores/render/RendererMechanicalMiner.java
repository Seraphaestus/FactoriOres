package seraphaestus.factoriores.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IModelData;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.block.BlockMechanicalMiner;

public class RendererMechanicalMiner extends KineticTileEntityRenderer {

	//PARTIAL_MODEL_TODOpublic static PartialModel DRILL_HEAD;
	
	public RendererMechanicalMiner(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return CreateClient.bufferCache.renderPartial(AllBlockPartials.MILLSTONE_COG, te.getBlockState());
	}
	
	@Override
	public boolean isGlobalRenderer(KineticTileEntity tileEntity) {
		return true;
	}
	
	@Override
	protected void renderSafe(KineticTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int light, int overlay) {
		super.renderSafe(tileEntity, partialTicks, matrixStack, bufferIn, light, overlay);
		
		BlockState state = tileEntity.getBlockState();
		IModelData data = tileEntity.getModelData();
		
		matrixStack.push();
		
		IBakedModel drillHead = RendererMiner.DRILL_HEAD;
		BlockModelRenderer bmr = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer();
		
		matrixStack.translate(0.0, -1.0, 0.0);
		
		if (state.get(BlockMechanicalMiner.ENABLED)) {
			//double animationTicks = ClientTickHandler.getTotalElapsedTicksInGame() + partialTicks;
			//float angle = (float)(animationTicks * speed % 360);
			float angle = ConfigHandler.CLIENT.staticDrills.get() ? 0 : getAngleForTe(tileEntity, tileEntity.getPos(), Axis.Y);
			matrixStack.translate(0.5, 0.0, 0.5);
			matrixStack.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(angle));
			matrixStack.translate(-0.5, 0.0, -0.5);
		}
		
		bmr.renderModel(matrixStack.peek(), 
						bufferIn.getBuffer(RenderTypeLookup.getEntityBlockLayer(state, false)),
						state,
						drillHead, 1, 1, 1, light, overlay, data);
		matrixStack.pop();
	}
	
}
