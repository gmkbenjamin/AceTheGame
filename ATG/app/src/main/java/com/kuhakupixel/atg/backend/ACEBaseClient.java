package com.kuhakupixel.atg.backend;

import android.content.Context;
import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.List;

public class ACEBaseClient {

    public class InvalidCommandException extends RuntimeException {
        public InvalidCommandException(String msg) {
            super(msg);
        }
    }

    String binaryPath = "";

    public ACEBaseClient(Context context, Binary.Type type) throws IOException {
        this.binaryPath = Binary.GetBinPath(context, type);
    }

    public void AssertValidCommand(List<String> out) throws InvalidCommandException {
        if (out.size() > 0) {
            if (out.get(0).equals("INVALID_COMMAND"))
                throw new InvalidCommandException("Invalid Command: " + String.join("\n", out));
        }
    }

    public List<String> RequestAsList(String requestCmd) throws InvalidCommandException {

        Log.i("ATG", String.format("Command to Engine: \"%s\"", requestCmd));
        //
        String[] cmdArr = new String[]{this.binaryPath, requestCmd};
        String cmdStr = String.join(" ", cmdArr);
        // run command
        Shell.Result result = Shell.cmd(cmdStr).exec();
        List<String> out = result.getOut();
        AssertValidCommand(out);
        Log.i("ATG", String.format("Output received from engine command: \"%s\"", requestCmd));
        return out;

    }

    public String Request(String requestCmd) throws InvalidCommandException {

        String outStr = String.join("\n", RequestAsList(requestCmd));
        return outStr;

    }

}
