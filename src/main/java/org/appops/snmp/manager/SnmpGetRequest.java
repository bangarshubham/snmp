package org.appops.snmp.manager;

import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpGetRequest {

  Snmp snmp = null;
  String address = null;

  /**
   * Constructor
   * 
   * @param add
   */
  public SnmpGetRequest(String add) {
    address = add;
  }

  public static void main(String[] args) throws IOException {
    /**
     * Port 161 is used for Read and Other operations.
     */
    SnmpGetRequest getRequest = new SnmpGetRequest("udp:127.0.0.1/161");
    getRequest.start();
    /**
     * OID - .1.3.6.1.2.1.1.1.0 => SystemDescription OID - .1.3.6.1.2.1.1.5.0 => SystemName
     */
    String[] SystemDescriptions = getRequest.getAsStrings(new OID[] {new OID(".1.3.6.1.2.1.1.1.0"),
        new OID(".1.3.6.1.2.1.1.5.0"), new OID(".1.3.6.1.2.1.1.9.1.3.1")});

    for (int i = 0; i < SystemDescriptions.length; i++) {
      System.out.println(SystemDescriptions[i]);
    }

  }

  /**
   * Start the Snmp session. If you forget the listen() method you will not get any answers because
   * the communication is asynchronous and the listen() method listens for answers.
   * 
   * @throws IOException
   */
  public void start() throws IOException {
    TransportMapping transport = new DefaultUdpTransportMapping();
    snmp = new Snmp(transport);
    transport.listen();
  }

  /**
   * Method which takes a single OID and returns the response from the agent as a String.
   * 
   * @param oid
   * @return
   * @throws IOException
   */
  public String[] getAsStrings(OID[] oids) throws IOException {
    ResponseEvent event = get(oids);

    Vector<VariableBinding> variableBindings =
        (Vector<VariableBinding>) event.getResponse().getVariableBindings();
    String[] variableStrings = new String[event.getResponse().getVariableBindings().size()];

    for (int i = 0; i < variableStrings.length; i++) {
      variableStrings[i] = variableBindings.get(i).getVariable().toString();
    }

    return variableStrings;
  }

  public String getAsString(OID oid) throws IOException, ParseException {
    ResponseEvent event = get(new OID[] {oid});
    return event.getResponse().get(0).getVariable().toString();
  }

  /**
   * This method is capable of handling multiple OIDs
   * 
   * @param oids
   * @return
   * @throws IOException
   */
  public ResponseEvent get(OID oids[]) throws IOException {
    PDU pdu = new PDU();
    for (OID oid : oids) {
      pdu.add(new VariableBinding(oid));
    }
    pdu.setType(PDU.GET);
    ResponseEvent event = snmp.send(pdu, getTarget(), null);
    if (event != null) {
      return event;
    }
    throw new RuntimeException("GET timed out");
  }

  /**
   * This method returns a Target, which contains information about where the data should be fetched
   * and how.
   * 
   * @return
   */
  private Target getTarget() {
    Address targetAddress = GenericAddress.parse(address);
    CommunityTarget target = new CommunityTarget();
    target.setCommunity(new OctetString("public"));
    target.setAddress(targetAddress);
    target.setRetries(2);
    target.setTimeout(1500);
    target.setVersion(SnmpConstants.version2c);
    return target;
  }
}
