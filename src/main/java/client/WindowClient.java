package client;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dominic.example.window.WindowGrpc;
import org.dominic.example.window.WindowStatus;

public class WindowClient implements ServiceObserver {

   protected ServiceDescription current;
   private final String serviceType;
   private final String name;
   private static final Logger logger = Logger.getLogger(GRPCHomeClient.class.getName());

   private ManagedChannel channel;
   private WindowGrpc.WindowBlockingStub blockingStub;

   /**
    * Constructor.
    */
   public WindowClient() {
      serviceType = "_window._udp.local.";
      name = "Home";
      jmDNSServiceTracker clientManager = jmDNSServiceTracker.getInstance();
      clientManager.register(this);

      serviceAdded(new ServiceDescription("18.202.21.182", 50021));
   }

   String getServiceType() {
      return serviceType;
   }

   void disable() {
      // no services exist for this client type
   }

   public List<String> serviceInterests() {
      List<String> interests = new ArrayList<String>();
      interests.add(serviceType);
      return interests;
   }

   public void serviceAdded(ServiceDescription service) {
      System.out.println("service added");
      current = service;
      channel = ManagedChannelBuilder.forAddress(service.getAddress(), service.getPort())
              .usePlaintext(true)
              .build();
      blockingStub = WindowGrpc.newBlockingStub(channel);
      openwindow();
   }

   public boolean interested(String type) {
      return serviceType.equals(type);
   }

   public String getName() {
      return name;
   }

   public void shutdown() throws InterruptedException {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
   }

   /**
    * Open the window.
    */
   public void openwindow() {
      try {

         new Thread() {
            public void run() {
               Empty request = Empty.newBuilder().build();

               Iterator<WindowStatus> response = blockingStub.open(request);
               while (response.hasNext()) {
                  System.out.println(response.next().toString());
               }
            }
         }.start();

         Empty request = Empty.newBuilder().build();
         WindowStatus status = blockingStub.getStatus(request);
         System.out.println("Opening the window " + status);

      } catch (RuntimeException e) {
         logger.log(Level.WARNING, "RPC failed", e);
         return;
      }
   }

   public static void main(String[] args) {
      new WindowClient();
   }

}
