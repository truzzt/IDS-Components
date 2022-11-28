package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.TokenVerificationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DapsTokenVerifierTest {

    //v2 DAT token
    private String dapsToken = "eyJ0eXAiOiJKV1QiLCJraWQiOiJkZWZhdWx0IiwiYWxnIjoiUlMyNTYifQ.eyJzZWN1cml0eVByb2ZpbGUiOiJpZHNjOkJBU0VfQ09OTkVDVE9SX1NFQ1VSSVRZX1BST0ZJTEUiLCJyZWZlcnJpbmdDb25uZWN0b3IiOiJodHRwOi8vaXNzdGJyb2tlci5kZW1vIiwiQHR5cGUiOiJpZHM6RGF0UGF5bG9hZCIsIkBjb250ZXh0IjoiaHR0cHM6Ly93M2lkLm9yZy9pZHNhL2NvbnRleHRzL2NvbnRleHQuanNvbmxkIiwidHJhbnNwb3J0Q2VydHNTaGEyNTYiOiIwZWU3M2RjMGViY2MwOTdhODEzMzRmNTRkNzkyMTc2NWZiMTNiOTFhN2EzYWE2YWMxZGVmYzc5Zjk1MzZkOTQzIiwic2NvcGVzIjpbImlkc2M6SURTX0NPTk5FQ1RPUl9BVFRSSUJVVEVTX0FMTCJdLCJhdWQiOiJpZHNjOklEU19DT05ORUNUT1JTX0FMTCIsImlzcyI6Imh0dHBzOi8vZGFwcy5haXNlYy5mcmF1bmhvZmVyLmRlIiwic3ViIjoiMTc6N0I6RUQ6MTg6NzM6RUI6RDA6NDc6NUM6QzM6MjU6NDk6NDc6MDQ6M0Q6QTI6OEI6NzI6ODY6QkY6a2V5aWQ6Q0I6OEM6Qzc6QjY6ODU6Nzk6QTg6MjM6QTY6Q0I6MTU6QUI6MTc6NTA6MkY6RTY6NjU6NDM6NUQ6RTgiLCJuYmYiOjE2MDY0ODA4ODUsImlhdCI6MTYwNjQ4MDg4NSwianRpIjoiTVRNMU9ETTNOVGMyTlRVd016UTVOVGswTXpJPSIsImV4cCI6MTYwNjQ4NDQ4NX0.mEf7SKgWNCdaK-PyCdBuEk8c0SGEgMwl6tpvCHTzO826wzPCR0Ps6TwjLRQyigcA7ffWm7O5oPOHF6B3FM5hCRSNEAsPlaMHNsX4t_qU8eh9Y0Dk57gtCfkz6FS2xMj-Foh7Zw7K2hTxoyWPfLvS69-dCj6s35qwuUdjIk4zAMS6QSkjxr5e_BjiU1hqCDWyHF9HSD8qcEb8WmIMGNTFzzFNYw_oAVlnyan7ylWt1mqu2-yq88V-07tlbEuKVyz2fgGrg97--Fm7PrB339Vkxw7SIj2B-YSXYhKjJjrUSESKwKpSAGD4n1HLCV35zbySNpEZ_aRhkkUfkEpUwzNapw";

    private String jwks = "{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",\"n\":\"uw6mFrdflXZTJgFOA5smDXC09SmpJWoGpyERZN" +
            "Ey31pKdsRGhTipR27j9irmmqihv7gIgzCnx6kIRNGI2u0oFQ5FgvO1xxgzcihdpF0CheOf9INgisPkq5hj8Ae_DYXkvjhQ6c6ak_ZYfj" +
            "0NpqyEPcJ5MLRmYGexMaMZmTbqDJvJl5JG3-bE3Ya21hTZYOxiSicpfFgJ30kn5aUIAtd05IZy7z1sDiVLtTXlLfe_ZQC4pnjFts-tc1" +
            "2sX9ihImnCkd0Wvz3CTZoyBSsc1TdBkb9m0C5tvg0fQP4QgF_zH2QoZnnrI52uAZ8MomWtY2lt3D0kkpR69pfVDJ7y3vN_ew\",\"kid" +
            "\":\"default\",\"d\":\"MOya6jQZ7uicxMLwmabZpAhHfBviXqPFi67tcrX4nUxrT3b0juGL_N_3Baqg1_ClJZTHvTBbobAHNqCrL" +
            "795jVBHRAHkGGevBPujylp9T3GCo5w9Au5Lf8oC0YPlJ29OraXjG9PqwCpdaN8lE-PpTpHvyppUcICOatIi3tzrCFK_a80h6R9YRklnJ" +
            "quaK2K1uc6mr6ImARHex6ZUlPYs5SmPk1xYotQhIrDwY6BZZ95cuiUM6zrisL-CRlsfZA0kTfteOnRK-8nrS_M2kYCzyCSxrhTRw4PFE" +
            "pIKMWuQjYS68pBIHCekbnEQ9Ia6WUGgj9MFx1oZZUflVHp8Sg3W-Q\",\"p\":\"9zN__RV0YgV5GubaHMNvR4E3kkOCgAgUHfbZIdiC" +
            "t3z0QGKFGsW1JRgPspe8QUc2t-N2RajgFaGLDj2XHvJ6d9CUBuYhu-vOvdAo0AVN_6AeW0ye4v1XjADAazcOpiLA6u2rBOnClowg4OZc" +
            "8CULkd5g2VU7bhruQ9a6Dyq6WxU\",\"q\":\"wbcdzrFxICo3E7JfifS9j-1MADXhJfel1G2T3-c7XhWtChtlDiH_wDzJrKRKQ0jKTs" +
            "Y9MZEsGIEvmjU7drMdik0n9oGRZGV0i9I4uu6J6Jr0Vv-eLjnLPK7RR_sbWyitNa-4eM5XwoXLXRnBdOJO1mYzye1ffa-Zk-SqaFz01E" +
            "8\",\"dp\":\"r97B9irnbD18lbkcAE7CwtqZ0MOa-r_EoDTY8g15olWR6KSvEgcLA2ME-Aif9MlXkqiPoqutEXam2fDTrT7SBqAsUGQ" +
            "2J95ybfTEtIqxnWYrcsUmXVPkj0SRoTXAhE5BdmK0w4AT4su7MIZ0JJ4WhybxPG45I_KhBT6ljNjXUKk\",\"dq\":\"qGqd3Z_jrIVH" +
            "UTaZXYWMcV9r3hr53lqKsD98ewO8V7YctVbP6xOgGPBAnnpPnmPgj9BGT5vZbIyUaEuzoSVkn2MpRWWslE6onw9sBwlwmOxvVlvymFni" +
            "2qVaimxEgmLBBJX7mfw7zIEqJ41G5moDBN_iUsKvbMSIj04U4Zs_uH0\",\"qi\":\"G67sfyzRCMDSgKJyC8nY3bpN5cJ80DsEq__fg" +
            "ey6yTLF6T0ihUiQQttk-FI_5A52CIqCgBTSUWHjMMtJnKVuwWATMTMjHaocm7ZCqLHFsUPu_fvm_cOcWsfeYRrTdSn6j6jLriloIGJvC" +
            "ikIBpFJbBzfDRuZBWbcOQShJdKvFnU\"}]}";


    @Test
    public void tokenValid_local() throws TokenVerificationException {
        DapsSecurityTokenVerifier tokenVerifier = new DapsSecurityTokenVerifier(new JWKSFromString(jwks));

        Token token = new TokenBuilder()._tokenValue_(dapsToken)._tokenFormat_(TokenFormat.JWT).build();
        tokenVerifier.setIgnoreJwtExpiration(true);
        tokenVerifier.verifySecurityToken(token, null);

        Assert.assertEquals(13, tokenVerifier.getClaimsCount());
    }

 /*   @Test
    public void testBedDatTest() throws TokenVerificationException {
        String dat = "eyJ0eXAiOiJKV1QiLCJraWQiOiJkZWZhdWx0IiwiYWxnIjoiUlMyNTYifQ.eyJzY29wZXMiOlsiaWRzYzpJRFNfQ09OTkVDVE9SX0FUVFJJQlVURVNfQUxMIl0sImF1ZCI6Imlkc2M6SURTX0NPTk5FQ1RPUlNfQUxMIiwiaXNzIjoiaHR0cDovL29tZWpkbjo0NTY3IiwibmJmIjoxNjM4MjkyOTMyLCJpYXQiOjE2MzgyOTI5MzIsImp0aSI6Ik9EUTNORGt5TWpRMk5qSXdNall3TnpRNU9RPT0iLCJleHAiOjE2MzgyOTY1MzIsInNlY3VyaXR5UHJvZmlsZSI6Imlkc2M6QkFTRV9TRUNVUklUWV9QUk9GSUxFIiwicmVmZXJyaW5nQ29ubmVjdG9yIjoiaHR0cDovL3Rlc3RpZHNhMTAuZGVtbyIsIkB0eXBlIjoiaWRzOkRhdFBheWxvYWQiLCJAY29udGV4dCI6Imh0dHBzOi8vdzNpZC5vcmcvaWRzYS9jb250ZXh0cy9jb250ZXh0Lmpzb25sZCIsInRyYW5zcG9ydENlcnRzU2hhMjU2IjoiMDU5NzJlYWQyN2FjMjJiMzdmYjNlZGMzMTA3MjNhM2EwZTc0YzAzNjBkOWMxMDlhM2I4YmQzOTBlNTVkMjJlMyIsInN1YiI6IkM2OjNFOkRFOjJCOkU3OjAwOjNGOjJBOjg0OkMwOjFEOkJGOjQxOjJDOkU2OjY1OjgyOjQ0OkU4OkFDOmtleWlkOkNCOjhDOkM3OkI2Ojg1Ojc5OkE4OjIzOkE2OkNCOjE1OkFCOjE3OjUwOjJGOkU2OjY1OjQzOjVEOkU4In0.A3J5xxza1FT1PVXpU6A8F31YYkkmc56f3LQgHRcYbgQWBSPS7LiK8ePQ4Fqy1ufkDuyw17ExsHtO7Rjnp2Fgby7BRn1K8ratV3wfPnPfsbBPjjn0MMEODT_QeD0Ow2QJ3mr_46l7-uiBkyMMg8RzIb3AEZUBV-PxWEQxSkIJAuOgtCzflnHOixkUgT7lio3dWJXxv9csPVzhwJ1Tgvt0yEiUz6sFbUC9jl6YZqXIowIo8IlnGjN_EUg--tBZtCbQQtqJ-gMMnddXFYRnoA5mV0TOzMUJi-7CweEUS9k89ByJ9n0xi--GgQK7w0Nu48tuYhnN9m7kTZU7sQe5obX-Hg";
        DynamicAttributeToken token = new DynamicAttributeTokenBuilder()
                ._tokenValue_(dat)
                ._tokenFormat_(TokenFormat.JWT)
                .build();

        List<String> trustedJwksHosts = new ArrayList<String>() {{add("omejdn");}};
        DapsSecurityTokenVerifier dapsSecurityTokenVerifier = new DapsSecurityTokenVerifier(new JWKSFromIssuer(trustedJwksHosts));
        dapsSecurityTokenVerifier.verifySecurityToken(token);
    } */
}
