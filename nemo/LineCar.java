import lejos.nxt.*;
import lejos.robotics.objectdetection.*;

/*
 * TODO: 
 * - Implement NXTMotor to use non regulated speed for a smoother ride - DONE
 * - Set up a good display diagnosis class	NOGOOD, DONE 
 * - Set both motors method	 (l_speed, r_speed) DONE, NOT USED
 * - Optimze getValues, only in diag? - DONE, 
 * - Regulate power-constant by checking main-loop exec time
 */ 

public class LineCar {
	LightSensor left;
	LightSensor right;
	NXTMotor m1;
	NXTMotor m2;
	NXTRegulatedMotor m1_reg;
	NXTRegulatedMotor m2_reg;
	UltrasonicSensor us;
	int currentState;
	int lPower;
	int rPower;
	int lValue;
	int rValue;
	float range;
	long whiletime;
	int count;

	public LineCar() throws Exception {
		LCD.drawString("LineCar started", 3, 2);
		Thread.sleep(1000);
		LCD.clear();

		// Using the non-regulated engines to speed up main-loop. 
		
		this.m1 = new NXTMotor(MotorPort.A);
		this.m2 = new NXTMotor(MotorPort.B);

		this.m1_reg = new NXTRegulatedMotor(MotorPort.A);
		this.m2_reg = new NXTRegulatedMotor(MotorPort.B);
		this.m1_reg.suspendRegulation();
		this.m2_reg.suspendRegulation();


		us = new UltrasonicSensor(SensorPort.S3);

		this.right = new LightSensor(SensorPort.S1, true);
		this.left = new LightSensor(SensorPort.S2, true);
	}

	public int getState() {
		// Two states 
		// I. Normal run
		// II. Obstacle detected

		this.count++;
		// Using getDistance on the Ultrasonic sensor instead of
		// a featureDetector speed up main-loop. 
		// To further speed up "getDistance" is only called every 10th loop. 
		if (this.count%10 == 0 && this.us.getDistance() < 15) {
			this.count = 0;
			return 2;
		}
		return 1;
	}

	public void motorSettings(int state) throws Exception {
		this.currentState = state;
		switch (state) {
			case 1: // Run as normal
					// Adaped scale with a proportional control system
					// Scale allows wheels run both forward and backward.
					//
					// TODO: Set the power constant ("4.3f") by checking how 
					// long time the main-loop takes to complete.
					float l = (left.readValue() - 46)*4.3f;
					float r = (right.readValue() - 48)*4.3f;
					m1.setPower(Math.round(l));
					m2.setPower(Math.round(r));
					break;
			case 2: // Obstacle
					this.avoidObstacle();
					break;
		}
	}

	private void avoidObstacle() throws Exception {
		// Stop unregulated motors
		Motor.A.stop();
		Motor.B.stop();
		// Reset tachocount to make sure the turns are correct.
		Motor.A.resetTachoCount();
		Motor.B.resetTachoCount();
		// Set speed and rotate
		Motor.A.setSpeed(200);
		Motor.B.setSpeed(200);
		Motor.A.rotate(200, true); // 2pi/4
		Motor.B.rotate(-150, false); // 2pi/4

		while((left.readValue()-right.readValue())<3){
			// Set speeds for inner/outer wheels. 
			// Inner ~= outer/2
			Motor.A.setSpeed(260);
			Motor.B.setSpeed(600);
			Motor.A.forward();
			Motor.B.forward();
	
			basicDiag();
			LCD.drawString("In obstacle-while", 0, 5);
		}
		LCD.clear();
		// Rotate to "follow-line"-position
		Motor.A.rotate(90, true);
		Motor.B.rotate(-90, false);
		Motor.A.suspendRegulation();
		Motor.B.suspendRegulation();
	}

	/*
	* Basic on-screen diagnostics.
	* Omit from main loop for increased main-loop performance.
	*/
	public void basicDiag() {
		this.lValue = left.readValue();
		this.rValue = right.readValue();
		this.lPower = m1.getPower();
		this.rPower = m2.getPower();

		LCD.drawString("M1:" + Integer.toString(this.lPower), 0, 0);
		LCD.drawString("M2:" + Integer.toString(this.rPower), 8, 0);

		LCD.drawString("S1:" + Integer.toString(this.rValue),0, 1);
		LCD.drawString("S2:" + Integer.toString(this.lValue),8, 1);
		LCD.drawString("RG:" + Float.toString(this.range), 0, 2);		
		//LCD.drawString("AG:" + Float.toString(this.angle), 8, 2);		
		LCD.drawString("ST:" + this.currentState, 0, 3);		
		LCD.drawString("WT:" + Long.toString(System.nanoTime()-this.whiletime), 8, 3);	
		LCD.drawString("TL:" + m1_reg.getTachoCount(), 0, 4);		
		LCD.drawString("TL:" + m1.getTachoCount(), 8, 4);

	}

}