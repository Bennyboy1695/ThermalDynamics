package cofh.thermaldynamics.duct.attachments.servo;

import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class ServoFluid extends ServoBase {

	public TileFluidDuct fluidDuct;

	public static float[] throttle = { 0.5F, 0.75F, 1F, 1.5F, 2F };

	@Override
	public int getId() {

		return AttachmentRegistry.SERVO_FLUID;
	}

	public ServoFluid(TileTDBase tile, byte side) {

		super(tile, side);
		fluidDuct = (TileFluidDuct) tile;
	}

	public ServoFluid(TileTDBase tile, byte side, int type) {

		super(tile, side, type);
		fluidDuct = (TileFluidDuct) tile;
	}

	public IFluidHandler theTile;

	@Override
	public void clearCache() {

		theTile = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		theTile = (IFluidHandler) tile;
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile instanceof IFluidHandler;
	}

	@Override
	public boolean canAddToTile(TileTDBase tileMultiBlock) {

		return tileMultiBlock instanceof TileFluidDuct;
	}

	@Override
	public void tick(int pass) {

		super.tick(pass);

		if (pass != 1 || fluidDuct.fluidGrid == null || !isPowered || !isValidInput) {
			return;
		}

		int maxInput = Math.min(fluidDuct.fluidGrid.myTank.getSpace(), (int) Math.ceil(fluidDuct.fluidGrid.myTank.fluidThroughput * throttle[type]));
		if (maxInput == 0) {
			return;
		}
		FluidStack returned = theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, false);

		if (fluidPassesFiltering(returned)) {
			if (fluidDuct.fluidGrid.myTank.getFluid() == null || fluidDuct.fluidGrid.myTank.getFluid().fluidID == 0) {
				fluidDuct.fluidGrid.myTank.setFluid(theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, true));
			} else if (fluidDuct.fluidGrid.myTank.getFluid().isFluidEqual(returned)) {
				fluidDuct.fluidGrid.myTank.getFluid().amount += theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, true).amount;
			}
		}
	}

	public boolean fluidPassesFiltering(FluidStack theFluid) {

		return theFluid != null && theFluid.fluidID != 0 && filter.allowFluid(theFluid);
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Duct.Type.FLUID, this);
	}

}