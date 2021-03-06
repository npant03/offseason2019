package frc.robot.subsystems.drive;

import com.ctre.phoenix.motorcontrol.ControlMode;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.EncoderFollower;
import jaci.pathfinder.modifiers.TankModifier;

public class PathfinderTestCommand extends Command{

    double leftOutput;
    double rightOutput;
    double desiredHeading;
    double gyroHeading;
    double gyroOutput;
    double angleDifference;

    EncoderFollower left;
    EncoderFollower right;

    public PathfinderTestCommand(){
        requires(Robot.driveBase);
    }

    @Override
    public void initialize(){

        SmartDashboard.putString("command status", "path generation");

        // fix the max velocity / acceleration at some point (in the nice enum!)
        // check drivebasefunctions?
        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, 
                                                        Trajectory.Config.SAMPLES_HIGH, 
                                                        0.02, // timestep on watchdog
                                                        2, // max velocity
                                                        .7, // max acceleration
                                                        30); // jerk because i kinda j wanna make this menos agresivo as they say
        Waypoint[] points = new Waypoint[] {
            // new Waypoint(3, 3, 45),
            new Waypoint(3, 3, Pathfinder.d2r(-90)),
            new Waypoint(0, 0, 0),
        };

        /* generating ideal trajectory for center of robot */
        Trajectory trajectory = Pathfinder.generate(points, config); 

        /* life isn't perfect and dimensionality exists. so.
           set up modifier to account for robot width */
        TankModifier modifier = new TankModifier(trajectory);
        modifier.modify(DriveBaseConstants.width.value);

        /* reset encoders because why not */
        Robot.getLeftMast().getSensorCollection().setQuadraturePosition(0, 10);
        Robot.getRightMast().getSensorCollection().setQuadraturePosition(0, 10); 

        /* sets up encoder follower objects for each side of the drive train */ 
        left = new EncoderFollower(modifier.getLeftTrajectory());
        right = new EncoderFollower(modifier.getRightTrajectory());

        left.configureEncoder(Robot.getLeftMast().getSelectedSensorPosition(0), 4096, 6);
        right.configureEncoder(Robot.getRightMast().getSelectedSensorPosition(0), 4096, 6);

        left.configurePIDVA(.2, 0.0, 0.0, 1 / DriveBaseConstants.maxVelocity.value, 0);
        right.configurePIDVA(.2, 0.0, 0.0, 1 / DriveBaseConstants.maxVelocity.value, 0);
    }

    @Override
    public void execute(){

        SmartDashboard.putString("command status", "pathfinder");

        leftOutput = left.calculate(Robot.getLeftMast().getSelectedSensorPosition(0));
        rightOutput = right.calculate(Robot.getRightMast().getSelectedSensorPosition(0));

        gyroHeading = Robot.gyro.getGyroAngle();
        SmartDashboard.putNumber("gyroHeading", gyroHeading);
        desiredHeading = Pathfinder.r2d(left.getHeading());

        // calculate delta angle 
        angleDifference = Pathfinder.boundHalfDegrees(desiredHeading - gyroHeading);

        // put it on scale from 0-360
        angleDifference = angleDifference % 360;
        
        // basically just a p controller
        gyroOutput = 0.8 * (-1.0/80.0) * angleDifference;

        // aux pid logic except it's aux p basically but shh detials                  (i did that on prupose)
        leftOutput += gyroOutput;
        rightOutput -= gyroOutput;

        // set motor powers
        Robot.getLeftMast().set(ControlMode.PercentOutput, 0.3*leftOutput);
        Robot.getRightMast().set(ControlMode.PercentOutput, 0.3*rightOutput);

        // print motor powers bc like that is useful
        SmartDashboard.putNumber("leftMastOutput", leftOutput);
        SmartDashboard.putNumber("rightMastOutput", rightOutput);
    }

    @Override
    public void end(){
        Robot.getLeftMast().set(ControlMode.PercentOutput, 0);
        Robot.getRightMast().set(ControlMode.PercentOutput, 0);
    }

    @Override
    public void interrupted(){
        end();
    }

    @Override
    public boolean isFinished(){
        return false;
    }
}