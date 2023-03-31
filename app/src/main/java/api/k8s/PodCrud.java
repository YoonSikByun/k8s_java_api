package api.k8s;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1ExecAction;
import io.kubernetes.client.openapi.models.V1Handler;
import io.kubernetes.client.openapi.models.V1KeyToPath;
import io.kubernetes.client.openapi.models.V1Lifecycle;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1ConfigMapKeySelector;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.openapi.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1NFSVolumeSource;
import io.kubernetes.client.openapi.models.V1EmptyDirVolumeSource;
import io.kubernetes.client.custom.Quantity;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Integer;;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
public class PodCrud {
    // public static makeVolume(List<Map<String, String>> volumeList) throws IOException, ApiException {
    //     String volumeType;

    //     for(Map<String, String> volumeMap:volumeList)
    //     {
    //         volumeType = volumeMap.get("")
    //     }
    // }

    public static V1Volume getVolumes(Map<String, Map<String, String>> volume) throws IOException, ApiException {
        Map<String, String> vol = volume.get("volumes");

        if (vol.containsKey("config_name"))
            return getConfigMapVolumes(volume);
        else if(vol.containsKey("claim_name"))
            return getPVCVolumes(volume);
        else if(vol.containsKey("nfs_server"))
            return getNFSVolumes(volume);
        else if(vol.containsKey("medium"))
            return getSHMVolumes(volume);

        return null;
    }

    public static V1Volume getSHMVolumes(Map<String, Map<String, String>> volume) throws IOException, ApiException {
        Map<String, String> vol = volume.get("volumes");

        return new V1Volume()
        .name(vol.get("volume_name"))
        .emptyDir(new V1EmptyDirVolumeSource().medium(vol.get("medium")));
    }

    public static V1Volume getNFSVolumes(Map<String, Map<String, String>> volume) throws IOException, ApiException {
        Map<String, String> vol = volume.get("volumes");
        boolean readOnly = Boolean.parseBoolean(vol.get("read_only"));

        return new V1Volume()
        .name(vol.get("volume_name"))
        .nfs(new V1NFSVolumeSource().server(vol.get("nfs_server")).path(vol.get("path")).readOnly(readOnly));
    }

    public static V1Volume getPVCVolumes(Map<String, Map<String, String>> volume) throws IOException, ApiException {
        Map<String, String> vol = volume.get("volumes");
        boolean readOnly = Boolean.parseBoolean(vol.get("read_only"));

        return new V1Volume()
        .name(vol.get("volume_name"))
        .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource()
        .claimName(vol.get("claim_name")).readOnly(readOnly));
    }

    //마운트될 pod 내부 디렉토리 지정
    public static V1Volume getConfigMapVolumes(Map<String, Map<String, String>> volume) throws IOException, ApiException {
        Map<String, String> vol = volume.get("volumes");

        String volName = vol.get("volume_name");
        String configName = vol.get("config_name");
        Map<String, String> mapV1KeyPaths = volume.get("V1KeyToPath");
        List<V1KeyToPath> listV1KeyPaths  = new ArrayList<V1KeyToPath>();

        for (Map.Entry<String, String> entry : mapV1KeyPaths.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            listV1KeyPaths.add(new V1KeyToPath().key(entry.getKey()).path(entry.getValue()));
        }

        return new V1Volume().name(volName).configMap(new V1ConfigMapVolumeSource().items(listV1KeyPaths).name(configName));
    }

    //마운트될 pod 내부 디렉토리 지정
    public static V1VolumeMount getVolumeMounts(Map<String, Map<String, String>> volume) throws IOException, ApiException {
        Map<String, String> vol = volume.get("volume_mounts");
        boolean readOnly = Boolean.parseBoolean(vol.get("read_only"));

        return (new V1VolumeMount().mountPath(vol.get("mount_path")).name(vol.get("name")).readOnly(readOnly));
    }

    //환경변수를 직접 설정할 때 사용
    public static List<V1EnvVar> getEnv(List<Map<String,String>> envSpec) throws IOException, ApiException {
        List<V1EnvVar> envItems = new ArrayList<V1EnvVar>();

        for (Map<String, String> env : envSpec) {
            final V1EnvVar envItem = new V1EnvVar();
            envItem.name(env.get("name")).value(env.get("key_value"));
            envItems.add(envItem);
        }

        return envItems;
    }

    //configMap에 있는 환경변수를 가져올 때 사용
    public static List<V1EnvVar> getEnvConfig(List<Map<String,String>> envSpec) throws IOException, ApiException {
        List<V1EnvVar> envItems = new ArrayList<V1EnvVar>();

        for (Map<String, String> env : envSpec) {
            final V1EnvVar envItem = new V1EnvVar();
            envItem.name(env.get("env_name"))
            .valueFrom(new V1EnvVarSource()
                .configMapKeyRef(new V1ConfigMapKeySelector()
                    .name(env.get("config_name"))
                    .key(env.get("key"))));
            envItems.add(envItem);
        }

        return envItems;
    }

    public static V1PodList getPodList(String namespaces) throws IOException, ApiException {
        // String fieldSelector = "metadata.namespace=mskim,status.phase=Running";
        String fieldSelector = "";
        if (!namespaces.isEmpty())
        {
            fieldSelector += "metadata.namespace=" + namespaces;
        }

        V1PodList podList = ApiConf.getCoreV1Api().listPodForAllNamespaces(null, null, fieldSelector, null, null, null, null, null, null, null);
        return podList;
    }

    public static boolean CreateDeployment(String deploymentName, String namespace, String requestCPU, String requestMemory, String limitCPU, String limitMemory,
                                            String gpu, Map<String, Integer> portMap, List<String> listPostStartCmd, List<String> listPreStopCmd, String containderImage
                                            ) throws IOException, ApiException {
        Map<String, Quantity> requests = new HashMap<>();
        Map<String, Quantity> limit = new HashMap<>();

        requests.put("cpu", Quantity.fromString(requestCPU));
        requests.put("memory", Quantity.fromString(requestMemory));

        limit.put("cpu", Quantity.fromString(limitCPU));
        limit.put("memory", Quantity.fromString(limitMemory));

        V1ResourceRequirements resource = new V1ResourceRequirements().requests(requests).limits(limit);
        
        String portName;
        Integer portNumber;

        List<V1ContainerPort> listPort = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : portMap.entrySet())
        {
            portName = entry.getKey();
            portNumber = entry.getValue();

            listPort.add(new V1ContainerPort().name(portName).containerPort(portNumber));
        }

        V1Handler postStart=null;
        if(listPreStopCmd.size() > 0)
            postStart = new V1Handler().exec(new V1ExecAction().command(listPostStartCmd));

        V1Handler preStop=null;
        if(listPreStopCmd.size() > 0)
            preStop = new V1Handler().exec(new V1ExecAction().command(listPreStopCmd));

        V1Lifecycle lifeCycle = new V1Lifecycle().postStart(postStart).preStop(preStop);
        // V1Container container = new V1Container().name(deploymentName).image(containderImage).ports(listPort).volumeMounts(null)
        return true;
    }
}
