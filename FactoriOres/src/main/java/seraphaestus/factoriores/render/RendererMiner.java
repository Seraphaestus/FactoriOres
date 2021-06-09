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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IModelData;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.block.BlockMiner;
import seraphaestus.factoriores.event.ClientTickHandler;
import seraphaestus.factoriores.tile.TileEntityMiner;
import seraphaestus.factoriores.tile.TileEntityOre;

public class RendererMiner extends TileEntityRenderer<TileEntityMiner> {

	public static IBakedModel DRILL_HEAD;
	
	public RendererMiner(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	public boolean isGlobalRenderer(TileEntityMiner tileEntity) {
		return true;
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
		
		if (RenderHelper.isLookingAt(tileEntity.getPos())) {
			renderRange(tileEntity, tileEntity.getRange(), matrixStack, bufferIn, light, overlay);
		}
	}
	
	public static void renderRange(@Nonnull TileEntity tileEntity, int range, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
		final double y = -1.99d;
		final BlockPos pos = tileEntity.getPos();
		for (int x = -range; x <= range; x++) {
			for (int z = -range; z <= range; z++) {
				TileEntity te = tileEntity.getWorld().getTileEntity(pos.add(x, y, z));
				if (!(te instanceof TileEntityOre)) continue;
				
				matrixStack.push();
				matrixStack.translate(x, y, z);

				RenderHelper.drawUpHighlight(matrixStack, buffer, RenderHelper.highlightColor, light);
				matrixStack.pop();
			}
		}
	}
}
