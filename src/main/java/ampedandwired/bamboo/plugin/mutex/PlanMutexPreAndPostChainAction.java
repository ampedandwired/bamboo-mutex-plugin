package ampedandwired.bamboo.plugin.mutex;

import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.plugins.PostChainAction;
import com.atlassian.bamboo.chains.plugins.PreChainAction;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlanMutexPreAndPostChainAction implements PreChainAction, PostChainAction {
    public static final Logger log = Logger.getLogger(PlanMutexPreAndPostChainAction.class);
    public static final String PLAN_MUTEX_KEY = "custom.bamboo.planMutex.list";
    private PlanManager planManager;

    private static ConcurrentMap<String, PlanKey> runningPlans = new ConcurrentHashMap<String, PlanKey>();

    @Override
    public void execute(@NotNull Chain chain, @NotNull ChainExecution chainExecution) throws Exception {
        PlanKey thisPlanKey = PlanKeys.getPlanKey(chain.getKey());
        Map<String, String> customConfig = chain.getBuildDefinition().getCustomConfiguration();
        String planMutexKey = customConfig.get(PLAN_MUTEX_KEY);
        if (planMutexKey != null) {
            PlanKey planToWaitFor = runningPlans.putIfAbsent(planMutexKey, thisPlanKey);
            while (planToWaitFor != null) {
                while (isPlanCurrentlyExecuting(planToWaitFor)) {
                    log.info("Waiting for plan " + planToWaitFor + " holding mutex '" + planMutexKey + "' to complete before executing " + thisPlanKey);
                    Thread.sleep(1000);
                }
                if (runningPlans.replace(planMutexKey, planToWaitFor, thisPlanKey)) {
                    planToWaitFor = null;
                } else {
                    planToWaitFor = runningPlans.putIfAbsent(planMutexKey, thisPlanKey);
                }
            }
            log.info("No plans with mutex '" + planMutexKey + "' are currently building. Proceeding with build of " + thisPlanKey);
        }
    }

    @Override
    public void execute(Chain chain, ChainResultsSummary chainResultsSummary, ChainExecution chainExecution) throws InterruptedException, Exception {
        PlanKey thisPlanKey = PlanKeys.getPlanKey(chain.getKey());
        Map<String, String> customConfig = chain.getBuildDefinition().getCustomConfiguration();
        String planMutexKey = customConfig.get(PLAN_MUTEX_KEY);
        if (planMutexKey != null) {
            runningPlans.remove(planMutexKey, thisPlanKey);
            log.info("Released mutex '" + planMutexKey + "' for build of " + thisPlanKey);
        }
    }

    private boolean isPlanCurrentlyExecuting(PlanKey planKey) {
        Plan plan = planManager.getPlanByKey(planKey);
        return plan.isExecuting() || plan.isActive();
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }
}
