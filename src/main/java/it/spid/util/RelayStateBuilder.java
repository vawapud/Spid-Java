package it.spid.util;



import java.util.Base64;

public class RelayStateBuilder {

    public String encodeRelayState(String relayState){
        String encodeRelayState = Base64.getEncoder().encodeToString(relayState.getBytes());

        return encodeRelayState;
    }

   /* public String buildRelayState(String relayState) {


        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        ByteArrayOutputStream byteArrayOutputStream = null;
        DeflaterOutputStream deflaterOutputStream = null;

        String encodeRelayState = null;

        try {

            byteArrayOutputStream = new ByteArrayOutputStream();
            deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
            deflaterOutputStream.write(relayState.getBytes());
            deflaterOutputStream.close();

            encodeRelayState = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);

            encodeRelayState = URLEncoder.encode(encodeRelayState, "UTF-8").trim();

        }catch(UnsupportedEncodingException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

        return encodeRelayState;
    }*/

    public String decodeRelayState(String relayState){
        byte[] decodeRelayState = Base64.getDecoder().decode(relayState);
        String decodedRelayState = new String(decodeRelayState);
        return decodedRelayState;
    }

}
