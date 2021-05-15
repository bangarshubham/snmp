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

public class SnmpGetBulkRequest {
  Snmp snmp = null;
  String address = null;


  public SnmpGetBulkRequest(String add) {
    address = add;
  }

  public static void main(String[] args) throws IOException, ParseException {

    SnmpGetBulkRequest bulkRequest = new SnmpGetBulkRequest("udp:127.0.0.1/161");
    bulkRequest.start();

    String[] listOfVariableString = bulkRequest.getAsStrings(new OID(".1.3.6.1.2.1.1.1.0"));

    for (int i = 0; i < listOfVariableString.length; i++) {
      System.out.println(listOfVariableString[i]);
    }

  }

  private void start() throws IOException {
    TransportMapping transport = new DefaultUdpTransportMapping();
    snmp = new Snmp(transport);
    transport.listen();
  }

  private String[] getAsStrings(OID oid) throws IOException, ParseException {
    ResponseEvent event = get(new OID[] {oid});

    Vector<VariableBinding> variableBindings =
        (Vector<VariableBinding>) event.getResponse().getVariableBindings();
    String[] variableStrings = new String[event.getResponse().getVariableBindings().size()];

    for (int i = 0; i < variableStrings.length; i++) {
      variableStrings[i] = variableBindings.get(i).getVariable().toString();
    }

    return variableStrings;
  }

  private ResponseEvent get(OID[] oids) throws IOException, ParseException {
    PDU pdu = new PDU();
    for (OID oid : oids) {
      pdu.add(new VariableBinding(oid));
    }
    pdu.setType(PDU.GETBULK);
    pdu.setMaxRepetitions(50);
    pdu.setNonRepeaters(0);
    ResponseEvent event = snmp.getBulk(pdu, getTarget());
    if (event != null) {
      return event;
    }
    throw new RuntimeException("GET timed out");
  }

  private Target getTarget() {
    Address targetAddress = GenericAddress.parse(address);
    CommunityTarget communityTarget = new CommunityTarget();
    communityTarget.setCommunity(new OctetString("public"));
    communityTarget.setAddress(targetAddress);
    communityTarget.setRetries(2);
    communityTarget.setTimeout(1500);
    communityTarget.setVersion(SnmpConstants.version2c);

    return communityTarget;
  }
}
