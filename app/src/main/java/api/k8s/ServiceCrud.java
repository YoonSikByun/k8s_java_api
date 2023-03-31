package api.k8s;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServiceCrud {
    public ServiceCrud() {}

    public static boolean CreateService(String namespace, String deploymentName, List<Integer> portInfos, String label, String svcType)  throws IOException, ApiException {
        if(svcType.isEmpty()) svcType="NodePort";

        List<V1ServicePort> listSvcPort = new ArrayList<V1ServicePort>();
        Map<String, String> svc_label = new HashMap<>();
 
        svc_label.put("app", label);

        for(int port : portInfos)
            listSvcPort.add(new V1ServicePort().port(port).name("port"+String.valueOf(port)));

        V1Service svcBody = new V1Service()
        .apiVersion(ApiConf.apiVersion)
        .kind("Service")
        .metadata(new V1ObjectMeta().name(deploymentName+"_svc").labels(svc_label))
        .spec(new V1ServiceSpec().selector(svc_label).ports(listSvcPort).type(svcType).externalName(null));

        ApiConf.getCoreV1Api().createNamespacedService(namespace, svcBody, null, null, null);

        return true;
    }
}
