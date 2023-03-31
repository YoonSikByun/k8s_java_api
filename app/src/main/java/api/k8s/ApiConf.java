package api.k8s;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1beta1Api; //This class is only supported up to version 11.0.2.
import io.kubernetes.client.openapi.apis.CustomObjectsApi;

import io.kubernetes.client.util.Config;
import java.io.IOException;

public class ApiConf {
    public static String apiAppVersion = "apps/v1";
    public static String apiVersion = "v1";

    public static void loadConfigMap() {
        // basePath;
        // apiKey;
        // verifyingSsl;
    }

    public static ApiClient getApiClient() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();

        // client.setBasePath(basePath); // K8S_API_URL
        // client.setApiKey(apiKey); //TOKEN
        // client.setVerifyingSsl(verifyingSsl); //False

        return client;
    }

    public static CoreV1Api getCoreV1Api() throws IOException, ApiException {
        return new CoreV1Api(getApiClient());    }

    public static AppsV1Api getAppsV1Api() throws IOException, ApiException {
        return new AppsV1Api(getApiClient());    }

    public static NetworkingV1Api getNetworkingV1Api() throws IOException, ApiException {
        return new NetworkingV1Api(getApiClient());    }

    public static NetworkingV1beta1Api getNetworkingV1beta1Api() throws IOException, ApiException {
        return new NetworkingV1beta1Api(getApiClient());    }

    public static CustomObjectsApi getCustomObjectsApi() throws IOException, ApiException {
        return new CustomObjectsApi(getApiClient());    }
}
