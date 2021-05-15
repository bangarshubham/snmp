package org.appops.snmp.manager;

import java.io.IOException;
import java.text.ParseException;
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

public class SnmpGetNextRequest {
  Snmp snmp = null;
  String address = null;


  public SnmpGetNextRequest(String add) {
    address = add;
  }

  public static void main(String[] args) throws IOException, ParseException {

    SnmpGetNextRequest getNextRequest = new SnmpGetNextRequest("udp:127.0.0.1/161");
    getNextRequest.start();

    String nextVariableName = getNextRequest.getAsString(new OID(".1.3.6.1.2.1.1.5.0"));
    System.out.println(nextVariableName);

  }

  private void start() throws IOException {
    TransportMapping transport = new DefaultUdpTransportMapping();
    snmp = new Snmp(transport);
    transport.listen();
  }

  private String getAsString(OID oid) throws IOException, ParseException {
    ResponseEvent event = get(new OID[] {oid});
    return event.getResponse().get(0).getVariable().toString();
  }

  private ResponseEvent get(OID[] oids) throws IOException, ParseException {
    PDU pdu = new PDU();
    for (OID oid : oids) {
      pdu.add(new VariableBinding(oid));
    }
    pdu.setType(PDU.GETNEXT);
    ResponseEvent event = snmp.getNext(pdu, getTarget());
    PDU responsePdu = event.getResponse();
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
