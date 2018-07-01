package np.tdfishers.learning;

import ec.util.MersenneTwisterFast;
import java.util.Collection;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.List;
import java.util.Map;

import static java.util.Collections.max;
import static java.util.function.Function.identity;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

/**
  This class supplies a learning module that implements the SARSA algorithm.
  It tries to be fairly generic and knows nothing about the particular
  simulation we're in. All it needs, besides the rng and the learning
  parameters, is a set of possible states, a list of possible actions, and a
  lambda that can return the current state.

  The actions are expected to have side-effects and to supply a double
  corresponding to the reward obtained as a result.

  We rely on the assumption that the reward and the new current state can be
  observed right after taking an action, which wouldn't be possible if that
  state was directly affected by the actions of other agents. (To allow this,
  we'd need to schedule the action value updating at some later ordering, and
  things would get a lot hairier.)

  We also assume that every action is possible in every state, which is the
  case for our particular problem, but wouldn't always be in general.
*/
public class Learning<S> {

  private final List<DoubleSupplier> actions;
  private final double epsilon;
  private final double alpha;
  private final double gamma;
  private final MersenneTwisterFast rng;

  /** The table holding all action values for each state */
  private final Map<S, Map<DoubleSupplier, Double>> qTable;

  /** Convenience method to get an action value */
  private double q(S state, DoubleSupplier action) {
    return qTable.get(state).get(action);
  };

  private DoubleSupplier nextAction;
  private Supplier<S> currentState;

  /** Returns a random action, with no regard to the current state or action values */
  public DoubleSupplier randomAction() {
    return actions.get(rng.nextInt(actions.size()));
  }

  /**
    Returns the best possible action in a state according
    to the values stored in the qTable.
  */
  public DoubleSupplier bestAction(S state) {
    return max(qTable.get(state).entrySet(), comparingByValue()).getKey();
  }

  /**
    @param states       A list of possible state an agent can be in.
    @param actions      A list of actions, which are lambdas returning rewards.
    @param currentState A lambda returning the current state of the agent.
    @param epsilon      The exploration rate.
    @param alpha        The learning rate.
    @param gamma        The discount factor for future states.
    @param rng          The random number generator.
  */
  public Learning(Collection<S> states, List<DoubleSupplier> actions,
    Supplier<S> currentState, double epsilon, double alpha, double gamma,
    MersenneTwisterFast rng) {
    this.actions = actions;
    this.currentState = currentState;
    this.epsilon = epsilon;
    this.alpha = alpha;
    this.gamma = gamma;
    this.rng = rng;

    // We init all our action values at 0.0...
    qTable = states.stream().collect(toMap(identity(), s ->
      actions.stream().collect(toMap(identity(), a -> 0.0))));
    // ...and thus start with a random action
    nextAction = randomAction();
  }

  /** The heart of the SARSA algorithm... */
  public void act() {
    S state = currentState.get();
    DoubleSupplier action = nextAction;
    double reward = action.getAsDouble();
    S nextState = currentState.get();
    nextAction = rng.nextDouble() < epsilon ?
      randomAction() :       // explore...
      bestAction(nextState); // ...or exploit
    double newValue =
      q(state, action) +
      alpha * (
        reward +
        gamma * q(nextState, nextAction) -
        q(state, action)
      );
    qTable.get(state).put(action, newValue);
  }

}
