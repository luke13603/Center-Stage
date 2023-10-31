package org.firstinspires.ftc.teamcode.hardware;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.Utility;

@Config
public class AutoScoringMech extends ScoringMech {

    ElapsedTime scoringWait = new ElapsedTime();
    ElapsedTime stackGrabbingWait = new ElapsedTime();

    public enum ScoringState{
        EXTENDING,
        WAITING_FOR_V4B_EXTEND,
        WAITING_FOR_CONE_DROP,
        WAITING_FOR_V4B_RETRACT,
        RETRACTING,
        DONE
    }
    ScoringState scoringState = ScoringState.EXTENDING;
    public ScoringState getScoringState() {
        return scoringState;
    }

    public enum StackGrabbingState{
        CREEPING,
        GRABBING,
        LIFTING,
        DONE
    }
    StackGrabbingState stackGrabbingState = StackGrabbingState.CREEPING;
    public StackGrabbingState getStackGrabbingState(){
        return stackGrabbingState;
    }

    public AutoScoringMech(HardwareMap hwmap){
        lift = new SingleMotorLift(hwmap);
        arm = new Arm(hwmap);
    }

    public void grabOffStackAsync(boolean hasCone){
        // You have to call updateLift while using this for it to work
        switch (stackGrabbingState){
            case CREEPING:
                setBottomState(false);
                retract();
                if (hasCone){
                    stackGrabbingWait.reset();
                    setBottomState(true);
                    stackGrabbingState = StackGrabbingState.GRABBING;
                }
                break;
            case GRABBING:
                if (stackGrabbingWait.milliseconds() > Arm.pixelActuationTime){
                    stackGrabbingWait.reset();
                    preMoveArm();
                    stackGrabbingState = StackGrabbingState.LIFTING;
                }
                break;
            case LIFTING:
                if (stackGrabbingWait.seconds() > 0.25){
                    stackGrabbingState = StackGrabbingState.DONE;
                }
                break;
        }
    }

    public boolean doneGrabbingOffStack(){
        return stackGrabbingState == StackGrabbingState.DONE;
    }

    public void grabOffStack(boolean hasCone){
        while (!(stackGrabbingState == StackGrabbingState.DONE)) {
            grabOffStackAsync(hasCone);
            update();
        }
    }
    // Have to call this before grabbing off the stack again to kick start the fsm
    public void resetStackGrabbingState(){
        stackGrabbingState = StackGrabbingState.CREEPING;
    }

    public void scoreAsync(double height){
        // You have to call updateLift while using this for it to work
        switch (scoringState){
            case EXTENDING:
                lift.setHeight(height);
                // Move on if the lift is all the way up
                if (Utility.withinErrorOfValue(lift.getHeight(), height, 5)) {
                    scoringWait.reset();
                    arm.pivotScore(); // Move the v4b over the junction
                    scoringState = ScoringState.WAITING_FOR_V4B_EXTEND;
                }
                break;
            case WAITING_FOR_V4B_EXTEND:
                if (scoringWait.seconds() > 0.65){ // Wait for the v4b to move all the way
                    setBottomState(false); // Drop the cone
                    scoringWait.reset();
                    scoringState = ScoringState.WAITING_FOR_CONE_DROP;
                }
                break;
            case WAITING_FOR_CONE_DROP:
                if (scoringWait.seconds() > 0.45){ // Wait for the cone to drop
                    arm.pivotGoToIntake(); // Move the v4b inside the bot
                    scoringWait.reset();
                    scoringState = ScoringState.WAITING_FOR_V4B_RETRACT;
                }
                break;
            case WAITING_FOR_V4B_RETRACT:
                if (scoringWait.milliseconds() > Arm.pivotActuationTime + 100){ // Wait for the v4b to retract all the way
                    scoringState = ScoringState.RETRACTING;
                }
                break;
            case RETRACTING:
                lift.setHeight(0); // Bring the lift down
                // Move on if the lift is all the way down
                if (Utility.withinErrorOfValue(lift.getHeight(), 0, 1)) {
                    scoringState = ScoringState.DONE; // Finish
                }
                break;
        }
    }

    // Uses and handles the bracer while scoring
    public void scoreWithBracer(double height){
        // You have to call updateLift while using this for it to work
        switch (scoringState){
            case EXTENDING:
                lift.setHeight(height);
                arm.pivotScore(); // Move the v4b over the junction
                // Move on if the lift is all the way up
                if (Utility.withinErrorOfValue(lift.getHeight(), height, 0.5)) {
                    scoringWait.reset();
                    scoringState = ScoringState.WAITING_FOR_V4B_EXTEND;
                }
                break;

            case WAITING_FOR_V4B_EXTEND:
                if (scoringWait.seconds() > 0.2){ // Wait for the v4b to move all the way
                    setBottomState(false); // Drop the cone
                    scoringWait.reset();
                    scoringState = ScoringState.WAITING_FOR_CONE_DROP;
                }
                break;

            case WAITING_FOR_CONE_DROP:
                if (scoringWait.seconds() > 0.3){ // Wait for the cone to drop
                    scoringWait.reset();
                    scoringState = ScoringState.WAITING_FOR_V4B_RETRACT;
                }
                break;

            case WAITING_FOR_V4B_RETRACT:
                armToIntakePos();

                if (scoringWait.milliseconds() > Arm.pivotActuationTime + 300){
                    scoringWait.reset();
                    retractLift();
                    scoringState = ScoringState.RETRACTING;
                }
                break;

            case RETRACTING:
                retractLift();
                // Move on if the lift is all the way down
                if (Utility.withinErrorOfValue(lift.getHeight(), 0, 1)) {
                    // Move this guy back in the bot so it doesn't get crunched
                    scoringState = ScoringState.DONE; // Finish
                }
                break;

            case DONE:
                break;
        }
    }

    public void score(double height){
        // While it isn't finished scoring, run an FSM
        while (!(scoringState == ScoringState.DONE)){
            scoreAsync(height);
            update();
        }
    }
    // Have to call this before scoring again to get the state machine to run
    public void resetScoringState(){
        scoringState = ScoringState.EXTENDING;
    }

    public boolean liftIsGoingDown(){
        return getScoringState() == ScoringState.WAITING_FOR_V4B_RETRACT;
    }
    public boolean liftIsMostlyDown(){
        return getScoringState() == ScoringState.RETRACTING;
    }

    public void displayAutoMechDebug(Telemetry telemetry){
        telemetry.addData("scoring state", scoringState.name());
        telemetry.addData("grabbing state", stackGrabbingState.name());
        displayDebug(telemetry);
    }
}
