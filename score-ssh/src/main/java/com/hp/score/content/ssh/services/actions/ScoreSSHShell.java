package com.hp.score.content.ssh.services.actions;

import com.hp.score.content.ssh.entities.ConnectionDetails;
import com.hp.score.content.ssh.entities.ExpectCommandResult;
import com.hp.score.content.ssh.entities.KeyFile;
import com.hp.score.content.ssh.entities.SSHShellInputs;
import com.hp.score.content.ssh.services.SSHService;
import com.hp.score.content.ssh.services.impl.SSHServiceImpl;
import com.hp.score.content.ssh.utils.Constants;
import com.hp.score.content.ssh.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ioanvranauhp on 11/5/2014.
 */
public class ScoreSSHShell extends SSHShellAbstract {

    /**
     * This method executes a ssh shell operations based on the sshShellInputs parameter
     * You ned to populate sshShellInputs parameter with the following values:
     host, port, username, password, privateKeyFile, command, characterSet, characterDelay, newlineCharacters, timeout,
     globalSessionObject, sessionObject, sessionId
     * @param sshShellInputs
     * @return
     */
    public Map<String, String> execute(SSHShellInputs sshShellInputs) {

        Map<String, String> returnResult = new HashMap<>();
        boolean providerAdded = addSecurityProvider();

        try {
            if (StringUtils.isEmpty(sshShellInputs.getCommand())) {
                throw new RuntimeException(COMMAND_IS_NOT_SPECIFIED_MESSAGE);
            }

            // default values
            int portNumber = StringUtils.toInt(sshShellInputs.getPort(), Constants.DEFAULT_PORT);
            sshShellInputs.setCharacterSet(StringUtils.toNotEmptyString(sshShellInputs.getCharacterSet(), Constants.DEFAULT_CHARACTER_SET));
            int characterDelayNumber = StringUtils.toInt(sshShellInputs.getCharacterDelay(), Constants.DEFAULT_WRITE_CHARACTER_TIMEOUT);
            String newline = StringUtils.toNewline(sshShellInputs.getNewlineCharacters());
            int timeoutNumber = StringUtils.toInt(sshShellInputs.getTimeout(), Constants.DEFAULT_TIMEOUT);

            // configure ssh parameters
            ConnectionDetails connection = new ConnectionDetails(sshShellInputs.getHost(), portNumber, sshShellInputs.getUsername(), sshShellInputs.getPassword());
            KeyFile keyFile = getKeyFile(sshShellInputs.getPrivateKeyFile(), sshShellInputs.getPassword());

            SSHService service = getSshService(sshShellInputs, connection, keyFile);

            // run the SSH Expect command
            ExpectCommandResult commandResult = service.runExpectCommand(
                    sshShellInputs.getCommand(),
                    sshShellInputs.getCharacterSet(),
                    newline,
                    characterDelayNumber,
                    Constants.DEFAULT_CONNECT_TIMEOUT,
                    timeoutNumber);

            // populate the results
            populateResult(returnResult, commandResult);
        } catch (Exception e) {
            populateResult(returnResult, e);
        } finally {
            if (providerAdded) {
                removeSecurityProvider();
            }
        }
        return returnResult;
    }

    private SSHService getSshService(SSHShellInputs sshShellInputs, ConnectionDetails connection, KeyFile keyFile) {
        SSHService service = null;
        if (sshShellInputs.getSessionId() != null) {
            // get the cached SSH session
            service = getFromCache(sshShellInputs.getSshGlobalSessionObject(), sshShellInputs.getSshSessionObject(), sshShellInputs.getSessionId());
        }
        if (service == null || !service.isConnected() || !service.isExpectChannelConnected()) {
            service = new SSHServiceImpl(connection, keyFile, Constants.DEFAULT_CONNECT_TIMEOUT, true);
        }
        return service;
    }


    private void populateResult(Map<String, String> returnResult, ExpectCommandResult commandResult) {
        returnResult.put(Constants.STDOUT, commandResult.getStandardOutput());
        returnResult.put(Constants.VISUALIZED, commandResult.getExpectXmlOutputs());
        returnResult.put(Constants.OutputNames.RETURN_RESULT, commandResult.getStandardOutput());
        returnResult.put(Constants.OutputNames.RETURN_CODE, Constants.ReturnCodes.RETURN_CODE_SUCCESS);
    }
}
