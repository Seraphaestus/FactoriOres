package seraphaestus.factoriores.render;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHelper {
	
	public static boolean isLookingAt(BlockPos pos) {
		BlockPos rayPos = null;
		RayTraceResult ray = Minecraft.getInstance().objectMouseOver;
		if (ray != null && ray.getType() == RayTraceResult.Type.BLOCK) {
			rayPos = ((BlockRayTraceResult) ray).getPos();
		}
		
		return pos.equals(rayPos);
	}
		
	public static final ResourceLocation blankTexture = new ResourceLocation("minecraft:textures/block/white_concrete.png");
	public static Color highlightColor = new Color(255, 255, 255, 150);
		
	public static void drawUpHighlight(MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, Color color, int combinedLight) {
		IVertexBuilder vertexBuilderBlockQuads = renderBuffer.getBuffer(RenderType.getEntityTranslucent(blankTexture));

		Matrix4f matrixPos = matrixStack.getLast().getMatrix(); // retrieves the current transformation matrix
		Matrix3f matrixNormal = matrixStack.getLast().getNormal(); // retrieves the current transformation matrix for
																	// the normal vector

		// we use the whole texture
		Vector2f bottomLeftUV = new Vector2f(0.0F, 1.0F);
		float UVwidth = 1.0F;
		float UVheight = 1.0F;

		// all faces have the same height and width
		final float WIDTH = 1.0F;
		final float HEIGHT = 1.0F;
		
		final Vector3d UP_FACE_MIDPOINT = new Vector3d(0.5, 1.0, 0.5);

		addFace(Direction.UP, matrixPos, matrixNormal, vertexBuilderBlockQuads,
				color, UP_FACE_MIDPOINT, WIDTH, HEIGHT, bottomLeftUV, UVwidth, UVheight, combinedLight);
	}

	private static void addFace(Direction whichFace,
			Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder renderBuffer,
			Color color, Vector3d centrePos, float width, float height,
			Vector2f bottomLeftUV, float texUwidth, float texVheight,
			int lightmapValue) {
		
		Vector3f leftToRightDirection, bottomToTopDirection;
		
		switch (whichFace) {
	      case NORTH: { // bottom left is east
	        leftToRightDirection = new Vector3f(-1, 0, 0);  // or alternatively Vector3f.XN
	        bottomToTopDirection = new Vector3f(0, 1, 0);  // or alternatively Vector3f.YP
	        break;
	      }
	      case SOUTH: {  // bottom left is west
	        leftToRightDirection = new Vector3f(1, 0, 0);
	        bottomToTopDirection = new Vector3f(0, 1, 0);
	        break;
	      }
	      case EAST: {  // bottom left is south
	        leftToRightDirection = new Vector3f(0, 0, -1);
	        bottomToTopDirection = new Vector3f(0, 1, 0);
	        break;
	      }
	      case WEST: { // bottom left is north
	        leftToRightDirection = new Vector3f(0, 0, 1);
	        bottomToTopDirection = new Vector3f(0, 1, 0);
	        break;
	      }
	      case UP: { // bottom left is southwest by minecraft block convention
	        leftToRightDirection = new Vector3f(-1, 0, 0);
	        bottomToTopDirection = new Vector3f(0, 0, 1);
	        break;
	      }
	      case DOWN: { // bottom left is northwest by minecraft block convention
	        leftToRightDirection = new Vector3f(1, 0, 0);
	        bottomToTopDirection = new Vector3f(0, 0, 1);
	        break;
	      }
	      default: {  // should never get here, but just in case;
	        leftToRightDirection = new Vector3f(0, 0, 1);
	        bottomToTopDirection = new Vector3f(0, 1, 0);
	        break;
	      }
	    }
		leftToRightDirection.mul(0.5F * width); // convert to half width
		bottomToTopDirection.mul(0.5F * height); // convert to half height

		// calculate the four vertices based on the centre of the face

		Vector3f bottomLeftPos = new Vector3f(centrePos);
		bottomLeftPos.sub(leftToRightDirection);
		bottomLeftPos.sub(bottomToTopDirection);

		Vector3f bottomRightPos = new Vector3f(centrePos);
		bottomRightPos.add(leftToRightDirection);
		bottomRightPos.sub(bottomToTopDirection);

		Vector3f topRightPos = new Vector3f(centrePos);
		topRightPos.add(leftToRightDirection);
		topRightPos.add(bottomToTopDirection);

		Vector3f topLeftPos = new Vector3f(centrePos);
		topLeftPos.sub(leftToRightDirection);
		topLeftPos.add(bottomToTopDirection);

		// texture coordinates are "upside down" relative to the face
		// eg bottom left = [U min, V max]
		Vector2f bottomLeftUVpos = new Vector2f(bottomLeftUV.x, bottomLeftUV.y);
		Vector2f bottomRightUVpos = new Vector2f(bottomLeftUV.x + texUwidth, bottomLeftUV.y);
		Vector2f topLeftUVpos = new Vector2f(bottomLeftUV.x + texUwidth, bottomLeftUV.y + texVheight);
		Vector2f topRightUVpos = new Vector2f(bottomLeftUV.x, bottomLeftUV.y + texVheight);

		Vector3f normalVector = whichFace.toVector3f(); // gives us the normal to the face

		addQuadVertex(matrixPos, matrixNormal, renderBuffer, bottomLeftPos, bottomLeftUVpos, normalVector, color, lightmapValue);
		addQuadVertex(matrixPos, matrixNormal, renderBuffer, bottomRightPos, bottomRightUVpos, normalVector, color, lightmapValue);
		addQuadVertex(matrixPos, matrixNormal, renderBuffer, topRightPos, topRightUVpos, normalVector, color, lightmapValue);
		addQuadVertex(matrixPos, matrixNormal, renderBuffer, topLeftPos, topLeftUVpos, normalVector, color, lightmapValue);
	}

	private static void addQuadVertex(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder renderBuffer,
			Vector3f pos, Vector2f texUV,
			Vector3f normalVector, Color color, int lightmapValue) {
		renderBuffer.pos(matrixPos, pos.getX(), pos.getY(), pos.getZ()) // position coordinate
				.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()) // color
				.tex(texUV.x, texUV.y) // texel coordinate
				.overlay(OverlayTexture.NO_OVERLAY) // only relevant for rendering Entities (Living)
				.lightmap(lightmapValue) // lightmap with full brightness
				.normal(matrixNormal, normalVector.getX(), normalVector.getY(), normalVector.getZ())
				.endVertex();
	}
}
