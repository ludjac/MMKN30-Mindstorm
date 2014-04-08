import lejos.nxt.*;
import java.util.*;

public class SumoCarUni {
	int speed;
	static int fValue;
	int state;
	NXTMotor m1;
	NXTMotor m2;
	boolean liftUp;
	static boolean gogogo;
	int surface;
	static int sonicRange;
	static int rSonicRange;
	static int lSonicRange;
	static int count;
	static boolean rotate = true;
	static int direction;

	public SumoCarUni() throws Exception {
		LCD.drawString("SumoCar started", 3, 2);
		Thread.sleep(1000);
		LCD.clear();
		this.m1 = new NXTMotor(MotorPort.A);
		this.m2 = new NXTMotor(MotorPort.B);
		Motor.C.resetTachoCount();
		this.state = 0;
		this.liftUp = false;
		this.gogogo = true;
		this.count = 0;
		this.direction = 1;

		//change depending on surface
		this.surface = 35;
	}

	private static class RSonic extends Thread {
		public void run(){
			UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);

			while(gogogo){
				int dist = us.getDistance();
				if (dist > 60) {
					// do nothing
					rSonicRange = 60;
				} else {
					rSonicRange = dist;
				}
			}
		}
	}

	private static class LSonic extends Thread {
		public void run(){
			UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);

			while(gogogo){
				int dist = us.getDistance();
				if (dist > 60) {
					// do nothing
					lSonicRange = 60;
				} else {
					lSonicRange = dist;
				}
			}
		}
	}

	private static class Light extends Thread {
		public void run(){
			LightSensor front = new LightSensor(SensorPort.S3, true);

			while(gogogo){
				fValue = front.readValue();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		SumoCarUni car = new SumoCarUni();
		new RSonic().start();
		new LSonic().start();
		new Light().start();
		Button.ENTER.waitForPress();

		while (gogogo) {
			// Basic display diagnostics 
			if (Button.ESCAPE.isDown()) {
				gogogo = false;
			}
			car.basicDiag();
			car.updateState();
			car.motorSettings();
		}
	}

	public void updateState() {
		sonicRange = Math.max(rSonicRange, lSonicRange);
		if (fValue < surface) {
			state = 3;
		} else {
			if (sonicRange > 50) {
				state = 1;
			} else {
				state = 2;
			}
		}
	}

	public void motorSettings() throws Exception {
		switch (state) {
			case 1:
				// search
				this.count ++;
				if (this.rotate) {
					m1.setPower(50*this.direction);
					m2.setPower(-50*this.direction);
					if (this.count % 120 == 0) {
						this.count = 0;
						this.rotate = false;
						if (this.direction > 0) {
							this.direction = -1;
						} else {
							this.direction = 1;
						}
					}
				}
				else {
					m1.setPower(60);
					m2.setPower(60);
					if (this.count % 260 == 0) {
						this.count = 0;
						this.rotate = true;
					}
				}
				break;
			case 2: 
				// run as normal
				float speed_float = -(sonicRange - 65)*1.7f;
				int speed = Math.round(speed_float);
				if (speed < 30) {
					this.speed = 30;
				} else {
					this.speed = speed;
				}

				if (rSonicRange < lSonicRange + 4) {
					//turn a little more left
					m1.setPower(speed);
					m2.setPower(speed + 10);	
				} else if (rSonicRange + 4 > lSonicRange) {
					//turn a little bit right
					m1.setPower(speed + 10);
					m2.setPower(speed);
				} else {
					m1.setPower(speed);
					m2.setPower(speed);
				}

				if (sonicRange < 20) {
					//power lift
					if (liftUp) {
						Motor.C.rotate(-90);
						liftUp = false;
					} else {
						// lift is down
						Motor.C.rotate(90);
						liftUp = true;
					}
				}
				break;

			case 3: 
				// edge
				m1.setPower(-80);
				m2.setPower(-80);
				Thread.sleep(1000);
				this.rotate = true;
				m1.setPower(50*this.direction);
				m2.setPower(-50*this.direction);
				Thread.sleep(1000);
				break;
		}
	}

	public void basicDiag() {
		//this.bValue = front.readValue();
		LCD.drawString("M:" + Integer.toString(speed), 0, 0);

		LCD.drawString("FS:" + Integer.toString(fValue),0, 1);
		LCD.drawString("son:" + Float.toString(sonicRange), 0, 2);

		LCD.drawString("LSon:" + Float.toString(lSonicRange), 0, 2);		
		LCD.drawString("rSon:" + Float.toString(rSonicRange), 8, 2);		
	

		LCD.drawString("ST:" + state, 0, 3);		
	}
}