package sh.wheel.gitops.agent.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.Group;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuotaLimitRangeService {

    List<HasMetadata> getQuotaAndLimitRangeObjectsFor(App app, Group group) {


        return new ArrayList<>();
    }

}
