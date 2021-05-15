package org.appops.snmp.manager;

import java.util.Date;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpInformSender {
  public static final String community = "public";

  // Sending Inform Request for dummy data
  public static final String Oid = ".1.3.6.1.2.1.1.9.0";

  // IP of Local Host
  public static final String ipAddress = "127.0.0.1";

  // Ideally Port 162 should be used to send receive Inform, any other available Port can be used
  public static final int port = 1024;

  public static void main(String[] args) {
    SnmpInformSender informSender = new SnmpInformSender();
    informSender.sendInformRequest();
  }

  /**
   * Send the Inform Request to the Localhost in specified port.
   */
  public void sendInformRequest() {
    try {
      // Create Transport Mapping
      TransportMapping transport = new DefaultUdpTransportMapping();
      transport.listen();

      // Create Target
      CommunityTarget cTarget = new CommunityTarget();
      cTarget.setCommunity(new OctetString(community));
      cTarget.setVersion(SnmpConstants.version2c);
      cTarget.setAddress(new UdpAddress(ipAddress + "/" + port));
      cTarget.setRetries(2);
      cTarget.setTimeout(5000);

      // Create PDU
      PDU pdu = new PDU();

      // need to specify the system up time
      pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(new Date().toString())));

      pdu.add(new VariableBinding(new OID(Oid), new OctetString("Interface Down")));
      pdu.setType(PDU.INFORM);

      // Send the PDU
      Snmp snmp = new Snmp(transport);
      ResponseEvent event = snmp.inform(pdu, cTarget);
      if (event != null) {
        System.out.println(
            "Inform message sent successfully... Check Snmp Inform receiver received or not ?");
      }
      snmp.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
