///////////////////////////////////////////////////////////////////////
// Name: ActionSalvation
// Desc: Set unit's life back to max, forgive their sins
// Date: 12/28/2009 - Gabe Jones
// TODO:
///////////////////////////////////////////////////////////////////////
package leo.shared.crusades;

// imports

import leo.shared.*;
import org.tinylog.Logger;

import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;


public class ActionSalvation implements Action {

    /////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////
    private final Unit owner;
    private final short targetType;
    private final short range;
    private short remaining;
    private final short max;
    private final short cost;
    private final String detail = Strings.ACTION_SALVATION_1;


    /////////////////////////////////////////////////////////////////
    // Constructor
    /////////////////////////////////////////////////////////////////
    public ActionSalvation(
            Unit newOwner) {
        max = 0;
        remaining = 0;
        cost = 1;
        owner = newOwner;
        targetType = TargetType.UNIT_AREA;
        range = 11;

        // temp
        remaining = max;
    }


    /////////////////////////////////////////////////////////////////
    // Perform the action on the client
    /////////////////////////////////////////////////////////////////
    public String perform(short target) {
        if (!validate(target)) return Strings.INVALID_ACTION;

        // Some temporary kill it code
        Unit victim = owner.getBattleField().getUnitAt(target);

        // Add some life
        victim.heal(owner);
        victim.forgive();

        // Generate a nifty effect
        owner.getCastle().getObserver().abilityUsed(owner.getLocation(), target, Constants.IMG_STAR);

        // The costs
        owner.deductActions(cost);
        remaining--;
        owner.getCastle().deductCommands(this, (byte) 1);

        owner.getBattleField().event(Event.WITNESS_ACTION, owner, victim, getType(), Event.NONE, Event.OK);

        return "" + owner.getName() + Strings.ACTION_SALVATION_2 + victim.getName();

    }


    /////////////////////////////////////////////////////////////////
    // Validate
    /////////////////////////////////////////////////////////////////
    public boolean validate(short target) {
        // Any actions left
        if (getRemaining() <= 0) return false;

        // Deployed yet?
        if (!owner.deployed()) return false;

        // If no target, all is well
        if (getTargetType() == TargetType.NONE) return true;

        // Search for the target
        Vector<Short> targets = getTargets();
        for (int i = 0; i < targets.size(); i++) {
            Short location = targets.elementAt(i);
            if (location.byteValue() == target) return true;
        }

        // Bad bad monkey
        Logger.error(Strings.INVALID_ACTION);
        return false;
    }


    /////////////////////////////////////////////////////////////////
    // Get the targets
    /////////////////////////////////////////////////////////////////
    public Vector<Short> getTargets() {
        Vector<Short> targets = owner.getBattleField().getTargets(owner, getTargetType(), getRange(), false, true, false, TargetType.FRIENDLY, owner.getCastle());
        refineTargets(targets);
        return targets;
    }


    /////////////////////////////////////////////////////////////////
    // Get the targets
    /////////////////////////////////////////////////////////////////
    public Vector<Short> getClientTargets() {
        Vector<Short> targets = owner.getBattleField().getTargets(owner, (byte) (getTargetType() - 6), getRange(), false, true, false, TargetType.FRIENDLY, owner.getCastle());
        refineTargets(targets);
        return targets;
    }


    /////////////////////////////////////////////////////////////////
    // Refine targets
    /////////////////////////////////////////////////////////////////
    private void refineTargets(Vector<Short> targets) {
        Stack<Short> removes = new Stack<Short>();
        Iterator<Short> it = targets.iterator();
        while (it.hasNext()) {
            Short byter = it.next();
            Unit unit = owner.getBattleField().getUnitAt(byter.byteValue());
            if (unit != null && !unit.isWounded() && !unit.isMurderer()) removes.add(byter);
        }

        while (removes.size() > 0)
            targets.remove(removes.pop());
    }


    /////////////////////////////////////////////////////////////////
    // Get the description
    /////////////////////////////////////////////////////////////////
    public String getDescription() {
        return Strings.ACTION_SALVATION_3;
    }


    /////////////////////////////////////////////////////////////////
    // Get the description
    /////////////////////////////////////////////////////////////////
    public String getRangeDescription() {
        return Strings.ANY_ALLY;
    }


    /////////////////////////////////////////////////////////////////
    // Get the cost description
    /////////////////////////////////////////////////////////////////
    public String getCostDescription() {
        String text;
        text = Strings.COSTS + getCost() + Strings.ACTION;
        return text;
    }


    /////////////////////////////////////////////////////////////////
    // Get remaining actions
    /////////////////////////////////////////////////////////////////
    public short getRemaining() {
        if (owner.noCost()) return 1;

        if ((owner.getCastle().getCommandsLeft() <= 0 && !owner.freelyActs()) || !owner.deployed()) return 0;

        if (getMax() <= 0)
            return (byte) (owner.getActionsLeft() / getCost());
        else
            return remaining;
    }


    /////////////////////////////////////////////////////////////////
    // Refresh
    /////////////////////////////////////////////////////////////////
    public void refresh() {
        remaining = max;
    }


    /////////////////////////////////////////////////////////////////
    // Start turn
    /////////////////////////////////////////////////////////////////
    public void startTurn() {
    }


    /////////////////////////////////////////////////////////////////
    // Gets
    /////////////////////////////////////////////////////////////////
    public String getName() {
        return Strings.ACTION_SALVATION_4;
    }

    public short getMax() {
        return max;
    }

    public short getCost() {
        return cost;
    }

    public short getRange() {
        return range;
    }

    public short getTargetType() {
        return targetType;
    }

    public Unit getOwner() {
        return owner;
    }

    public Unit getHiddenUnit() {
        return null;
    }

    public boolean passive() {
        return false;
    }

    public short getType() {
        return Action.SPELL;
    }

    public String getDetail() {
        return detail;
    }
}
