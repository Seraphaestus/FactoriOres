package seraphaestus.factoriores.util;

import java.util.Random;

import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class VecHelper {
	
	//methods copied from create.foundation.utility.VecHelper (for compat reasons)
	
	public static final Vector3d CENTER_OF_ORIGIN = new Vector3d(.5, .5, .5);
	
	public static Vector3d rotate(Vector3d vec, double deg, Axis axis) {
		if (deg == 0)
			return vec;
		if (vec == Vector3d.ZERO)
			return vec;

		float angle = (float) (deg / 180f * Math.PI);
		double sin = MathHelper.sin(angle);
		double cos = MathHelper.cos(angle);
		double x = vec.x;
		double y = vec.y;
		double z = vec.z;

		if (axis == Axis.X)
			return new Vector3d(x, y * cos - z * sin, z * cos + y * sin);
		if (axis == Axis.Y)
			return new Vector3d(x * cos + z * sin, y, z * cos - x * sin);
		if (axis == Axis.Z)
			return new Vector3d(x * cos - y * sin, y * cos + x * sin, z);
		return vec;
	}
	
	public static Vector3d offsetRandomly(Vector3d vec, Random r, float radius) {
		return new Vector3d(vec.x + (r.nextFloat() - .5f) * 2 * radius, vec.y + (r.nextFloat() - .5f) * 2 * radius,
			vec.z + (r.nextFloat() - .5f) * 2 * radius);
	}
	
	public static Vector3d getCenterOf(Vector3i pos) {
		if (pos.equals(Vector3i.NULL_VECTOR))
			return CENTER_OF_ORIGIN;
		return Vector3d.copy(pos)
			.add(.5f, .5f, .5f);
	}
}
