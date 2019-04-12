package client;

import com.google.protobuf.Empty;
import org.jpdna.grpchello.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Iterator;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dominic.example.window.WindowGrpc;
import org.dominic.example.window.WindowStatus;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class GRPCHomeClient {

}
