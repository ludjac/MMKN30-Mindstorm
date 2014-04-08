import lejos.nxt.*;

/* Lego LineFollowers, we are the robots BY: Ludde & Casper 
 */
public class nxt_line{

	public static void main(String[] args) throws Exception{
		LineCar car = new LineCar();
		Button.ENTER.waitForPress();
		car.whiletime = System.nanoTime();
		while(!Button.ESCAPE.isDown()) {
			// Print basic diag on disp
			car.basicDiag();

			// Get State
			int state = car.getState();
			
			// Set motor settings according to state
			car.motorSettings(state);

			/* SUPER FAST MOTORSPEEDZZ - Set motors to public in Car-class. 
			int l = (car.left.readValue() - 45)*5;
			int r = (car.right.readValue() - 45)*5;
			car.m1.setPower(l);
			car.m2.setPower(r);
			*/

			// time the while-loop
			car.whiletime = System.nanoTime();
		}
	}
}