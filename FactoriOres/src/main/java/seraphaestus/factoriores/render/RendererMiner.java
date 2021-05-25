package seraphaestus.factoriores.render;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IModelData;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.block.BlockMiner;
import seraphaestus.factoriores.event.ClientTickHandler;
import seraphaestus.factoriores.tile.TileEntityMiner;

public class RendererMiner extends TileEntityRenderer<TileEntityMiner> {

	public static IBakedModel DRILL_HEAD;
	
	public RendererMiner(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void render(@Nonnull TileEntityMiner tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int light, int overlay) {

		BlockState state = tileEntity.getBlockState();
		IModelData data = tileEntity.getModelData();
		int speed = ConfigHandler.CLIENT.staticDrills.get() ? 0 : 6;
		
		matrixStack.push();
		
		IBakedModel drillHead = DRILL_HEAD;
		BlockModelRenderer bmr = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer();
		
		matrixStack.translate(0.0, -1.0, 0.0);
		
		if (state.get(BlockMiner.ENABLED)) {
			double animationTicks = ClientTickHandler.getTotalElapsedTicksInGame() + partialTicks;
			float angle = (float)(animationTicks * speed % 360);
			matrixStack.translate(0.5, 0.0, 0.5);
			matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-angle));
			matrixStack.translate(-0.5, 0.0, -0.5);
		}
		
		bmr.renderModel(matrixStack.peek(), 
						bufferIn.getBuffer(RenderTypeLookup.getEntityBlockLayer(state, false)),
						state,
						drillHead, 1, 1, 1, light, overlay, data);
		matrixStack.pop();
	}
}
