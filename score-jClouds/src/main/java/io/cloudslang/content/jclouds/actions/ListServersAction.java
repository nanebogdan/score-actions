package io.cloudslang.content.jclouds.actions;

import com.hp.oo.sdk.content.annotations.Action;
import com.hp.oo.sdk.content.annotations.Output;
import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.annotations.Response;
import com.hp.oo.sdk.content.plugin.ActionMetadata.MatchType;
import com.hp.oo.sdk.content.plugin.ActionMetadata.ResponseType;
import io.cloudslang.content.jclouds.entities.inputs.CommonInputs;
import io.cloudslang.content.jclouds.entities.inputs.ListServersInputs;
import io.cloudslang.content.jclouds.entities.inputs.ServerIdentificationInputs;
import io.cloudslang.content.jclouds.entities.outputs.Outputs;
import io.cloudslang.content.jclouds.execute.ListServersExecutor;
import io.cloudslang.content.jclouds.utilities.ExceptionProcessor;

import java.util.Map;

/**
 * Created by persdana on 6/23/2015.
 */
public class ListServersAction {
    /**
     * Lists the server from a cloud region delimited by a delimiter.
     *
     * @param provider          The cloud provider on which you have the instance. Valid values: "amazon" or "openstack".
     * @param identityEndpoint  The endpoint to which first request will be sent. Example: "https://ec2.amazonaws.com" for amazon or "http://hostOrIp:5000/v2.0" for openstack.
     * @param identity          The username of your account or the Access Key ID. For openstack provider the required format is 'alias:username'.
     * @param credential        The password of the user or the Secret Access Key that correspond to the identity input.
     * @param region            The region from which to list servers. Ex: "RegionOne", "us-east-1". ListRegionAction can be used in order to get all regions.
     * @param proxyHost         The proxy server used to access the web site. If empty no proxy will be used.
     * @param proxyPort         The proxy server port.
     * @param delimiter         A delimiter separating the resulted servers.
     * @return
     */
    @Action(name = "List Servers",
            outputs = {
                    @Output(Outputs.RETURN_CODE),
                    @Output(Outputs.RETURN_RESULT),
                    @Output(Outputs.EXCEPTION)
            },
            responses = {
                    @Response(text = Outputs.SUCCESS, field = Outputs.RETURN_CODE, value = Outputs.SUCCESS_RETURN_CODE, matchType = MatchType.COMPARE_EQUAL, responseType = ResponseType.RESOLVED),
                    @Response(text = Outputs.FAILURE, field = Outputs.RETURN_CODE, value = Outputs.FAILURE_RETURN_CODE, matchType = MatchType.COMPARE_EQUAL, responseType = ResponseType.ERROR)
            }
    )
    public Map<String, String> execute(
            @Param(value = CommonInputs.PROVIDER, required = true) String provider,
            @Param(value = CommonInputs.ENDPOINT, required = true) String identityEndpoint,
            @Param(value = CommonInputs.IDENTITY) String identity,
            @Param(value = CommonInputs.CREDENTIAL) String credential,
            @Param(value = ServerIdentificationInputs.REGION) String region,
            @Param(value = CommonInputs.PROXY_HOST) String proxyHost,
            @Param(value = CommonInputs.PROXY_PORT) String proxyPort,
            @Param(value = ListServersInputs.DELIMITER) String delimiter
    ) {

        ListServersInputs listServersInputs = new ListServersInputs(provider, identity, credential, identityEndpoint, proxyHost, proxyPort, region, delimiter);

        try {
            return new ListServersExecutor().execute(listServersInputs);
        } catch (Exception e) {
            return ExceptionProcessor.getExceptionResult(e);
        }
    }
}
