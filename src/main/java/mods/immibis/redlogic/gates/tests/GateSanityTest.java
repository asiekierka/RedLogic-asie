package mods.immibis.redlogic.gates.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import mods.immibis.redlogic.chips.generated.CCOFactory;
import mods.immibis.redlogic.gates.EnumGates;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;

import org.junit.Test;

public class GateSanityTest {
	/** Tests that the outputs of a gate don't depend on any non-inputs */
	@Test
	public void testGateNonInputConsistency() {
		short ON = 255, OFF = 0;
		
		for(EnumGates type : EnumGates.VALUES) {
			
			GateLogic logic = type.createLogic();
			
			if(!(logic instanceof GateLogic.Stateless) || (logic instanceof GateLogic.WithBundledConnections)) {
				System.out.println("Consistency testing skipped "+type);
				continue;
			}
			
			System.out.println("Consistency testing "+type);
			
			for(int gateSettings = 0; gateSettings < 65536; gateSettings++) {
				int inputMask = 0;
				for(int k = 0; k < 4; k++)
					if(logic.getInputID(k, gateSettings))
						inputMask |= (1 << k);
				
				int[] outputValueMasks = new int[16];
				
				for(int k = 0; k < 16; k++) {
					short[] inputs = new short[] {(k&1)!=0?ON:OFF, (k&2)!=0?ON:OFF, (k&4)!=0?ON:OFF, (k&8)!=0?ON:OFF};
					short[] outputs = new short[4];
					logic.update(inputs, outputs, gateSettings);
					
					for(int i = 0; i < 4; i++)
						if(outputs[i] != 0)
							outputValueMasks[k] |= (1 << i);
				}
				
				// now: check output values don't depend on non-inputs
				{
					int[] outputValueByInputs = new int[16];
					for(int k = 0; k < 16; k++) {
						if(k == (k & inputMask))
							outputValueByInputs[k] = outputValueMasks[k];
						else if(outputValueByInputs[k&inputMask] != outputValueMasks[k])
							fail("Gate type "+type+"; settings "+gateSettings+"; output for input sets "+k+" and "+(k&inputMask)+" differs but inputs are equal");
					}
				}
			}
		}
	}
	
	private int[] getGateSettingsToTest(EnumGates type) {
		// TODO
		return new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
	}
	
	private File getDataDir() {
		File f = new File("GateSanityTest-temp");
		if(f.isDirectory())
			for(String s : f.list())
				if(!new File(f, s).delete())
					throw new RuntimeException("couldn't delete "+s);
		else
			f.mkdirs();
		return f;
	}
	
	/** Tests that compiled gates behave the same as interpreted gates */
	@Test
	public void testGateCompilerConsistency() throws IOException {
		short ON = 255, OFF = 0;
		
		CCOFactory.instance = new CCOFactory(getDataDir());
		
		boolean failed = false;
		
		try {
			for(EnumGates type : EnumGates.VALUES) {
				
				GateLogic logic = type.createLogic();
				final GateCompiler compiler = type.getCompiler();
				
				if(/*!(logic instanceof GateLogic.Stateless) || (logic instanceof GateLogic.WithBundledConnections) ||*/ compiler == null) {
					System.out.println("Compiler testing skipped "+type);
					if(compiler != null) {
						System.out.println("   EVEN THOUGH IT HAS A COMPILER!");
						failed = true;
					}
					continue;
				}
				
				System.out.println("Compiler testing "+type);
				
				boolean printedFailureThisType = false;
				
				for(final int gateSettings : getGateSettingsToTest(type)) {
					
					GateTestbed testbed = new GateTestbed(type, gateSettings, true);
					
					int[] ins0 = testbed.getTestInputs(0);
					int[] ins1 = testbed.getTestInputs(1);
					int[] ins2 = testbed.getTestInputs(2);
					int[] ins3 = testbed.getTestInputs(3);
					
					for(int in0 : ins0) {
						testbed.setInput(0, in0);
						for(int in1 : ins1) {
							testbed.setInput(1, in1);
							for(int in2 : ins2) {
								testbed.setInput(2, in2);
								for(int in3 : ins3) {
									testbed.setInput(3, in3);
									testbed.resetGate();
									testbed.tickGate();
									testbed.tickGate();
									testbed.tickGate();
									
									String inputString = in0+"/"+in1+"/"+in2+"/"+in3; 
									
									boolean failedThisInputCombo = false;
									
									for(int dir = 0; dir < 4; dir++) {
										int fromLogic = testbed.getOutputOrInputFromLogic(dir);
										int fromCompiler = testbed.getOutputOrInputFromCompiler(dir);
										
										if(fromLogic != fromCompiler) {
											if(!printedFailureThisType)
												System.out.println(type+" for input "+inputString+", setting "+gateSettings+", side "+dir+": logic outputs "+fromLogic+", compiler outputs "+fromCompiler);
											failedThisInputCombo = true;
										}
										//else System.out.println(type+" for input "+inputString+", setting "+gateSettings+", side "+dir+": both output "+fromLogic);
									}
									
									//printedFailureThisType |= failedThisInputCombo;
									failed |= failedThisInputCombo;
								}
							}
						}
					}
				}
			}
		} finally {
			CCOFactory.instance = null;
			if(!failed)
				getDataDir().delete();
		}
		
		if(failed)
			fail("see console");
	}
}
