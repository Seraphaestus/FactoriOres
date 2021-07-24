package seraphaestus.factoriores.tile;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

public class InstanceMinerCog extends SingleRotatingInstance {

	public InstanceMinerCog(MaterialManager<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
	protected Instancer<RotatingData> getModel() {
        return getRotatingMaterial().getModel(AllBlockPartials.MILLSTONE_COG, tile.getBlockState());
    }
}