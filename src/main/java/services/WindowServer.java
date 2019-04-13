package services;

import org.jpdna.grpchello.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import io.grpc.stub.StreamObserver;
import java.util.Timer;
import java.util.TimerTask;

import java.util.logging.Logger;
import org.dominic.example.window.WindowGrpc;
import org.dominic.example.window.WindowStatus;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class WindowServer {

    private static final Logger logger = Logger.getLogger(WindowServer.class.getName());

    /* The port on which the server should run */
    private int port = 50021;
    private Server server;

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new WindowImpl())
                .build()
                .start();
        JmDNSRegistrationHelper helper = new JmDNSRegistrationHelper("Home", "_window._udp.local.", "", port);
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                WindowServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon
     * threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws Exception {
        final WindowServer server = new WindowServer();
        server.start();
        server.blockUntilShutdown();
    }

    private class WindowImpl extends WindowGrpc.WindowImplBase {

        private int winlevel = 0;

        public WindowImpl() {
            String name = "Home";
            String serviceType = "_window._udp.local.";
        }

        @Override
        public void open(com.google.protobuf.Empty request,
                io.grpc.stub.StreamObserver<org.dominic.example.window.WindowStatus> responseObserver) {
            Timer t = new Timer();
            t.schedule(new RemindTask(responseObserver), 0, 2000);

        }

        @Override
        public void getStatus(com.google.protobuf.Empty request,
                io.grpc.stub.StreamObserver<org.dominic.example.window.WindowStatus> responseObserver) {
            responseObserver.onNext(WindowStatus.newBuilder().setPercentage(winlevel).build());
            responseObserver.onCompleted();
        }

        class RemindTask extends TimerTask {

            StreamObserver<WindowStatus> o;

            public RemindTask(StreamObserver<WindowStatus> j) {
                o = j;
            }

            @Override
            public void run() {
                if (winlevel < 10) {
                    winlevel += 1;
                    WindowStatus status = WindowStatus.newBuilder().setPercentage(winlevel).build();
                    o.onNext(status);
                } else {
                    o.onCompleted();
                    this.cancel();
                }
            }
        }
    }
}
