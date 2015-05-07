package mods.immibis.redlogic;

import mods.immibis.core.api.util.Dir;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RotatedTessellator {
	public Tessellator base;
	public int front, side;
	public double x, y, z;
	public boolean flipped;
	
	public boolean useFaceShading;
	public float r, g, b;
	
	private double[] quadBuffer = new double[20];
	private int bufferPos;
	
	// TODO rotationLookup is pointless when we could just put the numbers directly in actuallyAddVertexWithUV
	private static int[][] rotationLookup = {
		{9, 9, 0, 3, 2, 1},
		{9, 9, 0, 3, 1, 2},
		{3, 0, 9, 9, 2, 1},
		{3, 0, 9, 9, 1, 2},
		{3, 0, 1, 2, 9, 9},
		{3, 0, 2, 1, 9, 9},
	};
	
	private void rotate(Vec3 v) {
		double temp;
		double x = v.xCoord, y = v.yCoord, z = v.zCoord;
		switch(side) {
		case Dir.NX: case Dir.PX:
			// x,y,z = y,z,x
			temp = y; y = z; z = x; x = temp;
			switch(rotationLookup[side][front]) {
			case 0: y=1-y; z=1-z; break;
			case 3: break;
			case 2: temp=y; y=z; z=1-temp; break;
			case 1: temp=y; y=1-z; z=temp; break;
			}
			if(side == Dir.PX) {
				x = 1-x;
				z = 1-z;
			}
			break;
		case Dir.NY: case Dir.PY:
			switch(rotationLookup[side][front]) {
			case 3: z=1-z; x=1-x; break;
			case 0: break;
			case 1: temp=z; z=x; x=1-temp; break;
			case 2: temp=z; z=1-x; x=temp; break;
			}
			if(side == Dir.PY) {
				y=1-y;
				x=1-x;
			}
			break;
		case Dir.NZ: case Dir.PZ:
			// x,y,z = x,z,y
			temp = z; z = y; y = temp;
			switch(rotationLookup[side][front]) {
			case 0: y=1-y; x=1-x; break;
			case 3: break;
			case 2: temp=y; y=x; x=1-temp; break;
			case 1: temp=y; y=1-x; x=temp; break;
			}
			if(side == Dir.PZ) {
				z = 1-z;
				//y = 1-y;
			} else
				x = 1-x;
			break;
		}
		v.xCoord = x;
		v.yCoord = y;
		v.zCoord = z;
	}
	
	private void actuallyAddVertexWithUV(double x, double y, double z, double u, double v) {
		double temp;
		switch(side) {
		case Dir.NX: case Dir.PX:
			// x,y,z = y,z,x
			temp = y; y = z; z = x; x = temp;
			switch(rotationLookup[side][front]) {
			case 0: y=1-y; z=1-z; break;
			case 3: break;
			case 2: temp=y; y=z; z=1-temp; break;
			case 1: temp=y; y=1-z; z=temp; break;
			}
			if(side == Dir.PX) {
				x = 1-x;
				z = 1-z;
			}
			break;
		case Dir.NY: case Dir.PY:
			switch(rotationLookup[side][front]) {
			case 3: z=1-z; x=1-x; break;
			case 0: break;
			case 1: temp=z; z=x; x=1-temp; break;
			case 2: temp=z; z=1-x; x=temp; break;
			}
			if(side == Dir.PY) {
				y=1-y;
				x=1-x;
			}
			break;
		case Dir.NZ: case Dir.PZ:
			// x,y,z = x,z,y
			temp = z; z = y; y = temp;
			switch(rotationLookup[side][front]) {
			case 0: y=1-y; x=1-x; break;
			case 3: break;
			case 2: temp=y; y=x; x=1-temp; break;
			case 1: temp=y; y=1-x; x=temp; break;
			}
			if(side == Dir.PZ) {
				z = 1-z;
				//y = 1-y;
			} else
				x = 1-x;
			break;
		}
		base.addVertexWithUV(x+this.x, y+this.y, z+this.z, u, v);
	}

	public void addVertexWithUV(double x, double y, double z, double u, double v) {
		if(flipped)
			x=1-x;
		
		quadBuffer[bufferPos++] = x;
		quadBuffer[bufferPos++] = y;
		quadBuffer[bufferPos++] = z;
		quadBuffer[bufferPos++] = u;
		quadBuffer[bufferPos++] = v;
		
		if(bufferPos == 20) {
			bufferPos = 0;
			
			if(flipped) {
				// swap vertex order
				actuallyAddVertexWithUV(quadBuffer[15], quadBuffer[16], quadBuffer[17], quadBuffer[18], quadBuffer[19]);
				actuallyAddVertexWithUV(quadBuffer[10], quadBuffer[11], quadBuffer[12], quadBuffer[13], quadBuffer[14]);
				actuallyAddVertexWithUV(quadBuffer[ 5], quadBuffer[ 6], quadBuffer[ 7], quadBuffer[ 8], quadBuffer[ 9]);
				actuallyAddVertexWithUV(quadBuffer[ 0], quadBuffer[ 1], quadBuffer[ 2], quadBuffer[ 3], quadBuffer[ 4]);
			} else {
				// don't swap vertex order
				actuallyAddVertexWithUV(quadBuffer[ 0], quadBuffer[ 1], quadBuffer[ 2], quadBuffer[ 3], quadBuffer[ 4]);
				actuallyAddVertexWithUV(quadBuffer[ 5], quadBuffer[ 6], quadBuffer[ 7], quadBuffer[ 8], quadBuffer[ 9]);
				actuallyAddVertexWithUV(quadBuffer[10], quadBuffer[11], quadBuffer[12], quadBuffer[13], quadBuffer[14]);
				actuallyAddVertexWithUV(quadBuffer[15], quadBuffer[16], quadBuffer[17], quadBuffer[18], quadBuffer[19]);
			}
		}
		
		
	}
	
	private float normx, normy, normz;
	
	public void setNormal(float x, float y, float z) {
		if(flipped) x = -x;
		normx = x; normy = y; normz = z;
		float temp;
		switch(side) {
		case Dir.NX: case Dir.PX:
			// x,y,z = y,z,x
			temp = y; y = z; z = x; x = temp;
			switch(rotationLookup[side][front]) {
			case 0: y=-y; z=-z; break;
			case 3: break;
			case 2: temp=y; y=z; z=-temp; break;
			case 1: temp=y; y=-z; z=temp; break;
			}
			if(side == Dir.PX) {
				x = -x;
				z = -z;
			}
			break;
		case Dir.NY: case Dir.PY:
			switch(rotationLookup[side][front]) {
			case 3: z=-z; x=-x; break;
			case 0: break;
			case 1: temp=z; z=x; x=-temp; break;
			case 2: temp=z; z=-x; x=temp; break;
			}
			if(side == Dir.PY) {
				y=-y;
				x=-x;
			}
			break;
		case Dir.NZ: case Dir.PZ:
			// x,y,z = x,z,y
			temp = z; z = y; y = temp;
			switch(rotationLookup[side][front]) {
			case 0: y=-y; x=-x; break;
			case 3: break;
			case 2: temp=y; y=x; x=-temp; break;
			case 1: temp=y; y=-x; x=temp; break;
			}
			if(side == Dir.PZ) {
				z = -z;
				//y = -y;
			} else
				x = -x;
			break;
		}
		if(useFaceShading) {
			if(y > 0)
				base.setColorOpaque_F(r*1.0f, g*1.0f, b*1.0f);
			else if(z != 0)
				base.setColorOpaque_F(r*0.8f, g*0.8f, b*0.8f);
			else if(x != 0)
				base.setColorOpaque_F(r*0.6f, g*0.6f, b*0.6f);
			else // y < 0
				base.setColorOpaque_F(r*0.5f, g*0.5f, b*0.5f);
		}
		base.setNormal(x, y, z);
	}

	public AxisAlignedBB rotate(AxisAlignedBB bb) {
		
		Vec3 v = Vec3.createVectorHelper(bb.minX, bb.minY, bb.minZ);
		rotate(v);
		bb.minX = v.xCoord; bb.minY = v.yCoord; bb.minZ = v.zCoord;
		
		v.xCoord = bb.maxX; v.yCoord = bb.maxY; v.zCoord = bb.maxZ;
		rotate(v);
		bb.maxX = v.xCoord; bb.maxY = v.yCoord; bb.maxZ = v.zCoord;
		
		double temp;
		if(bb.maxX < bb.minX) {temp = bb.minX; bb.minX = bb.maxX; bb.maxX = temp;}
		if(bb.maxY < bb.minY) {temp = bb.minY; bb.minY = bb.maxY; bb.maxY = temp;}
		if(bb.maxZ < bb.minZ) {temp = bb.minZ; bb.minZ = bb.maxZ; bb.maxZ = temp;}
		
		return bb;
	}

	public AxisAlignedBB rotate(double x1, double x2, double y1, double y2, double z1, double z2) {
		return rotate(AxisAlignedBB.getBoundingBox(x1, y1, z1, x2, y2, z2));
	}

	public void setColour(int rgb) {
		r = ((rgb >> 16) & 255) / 255.0f;
		g = ((rgb >> 8) & 255) / 255.0f;
		b = (rgb & 255) / 255.0f;
		if(useFaceShading)
			setNormal(normx, normy, normz);
		else
			base.setColorOpaque_I(rgb);
	}
}