package com.nms.nbi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "nms.simulation.enabled=false")
@AutoConfigureMockMvc
class NbiApiTest {

    @Autowired MockMvc mvc;

    @Test
    void listsAllDevices() throws Exception {
        mvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void returnsDeviceStatus() throws Exception {
        mvc.perform(get("/api/devices/R1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.device").value("R1"))
                .andExpect(jsonPath("$.status").value("ONLINE"))
                .andExpect(jsonPath("$.cpuUsage").value(42.0));
    }

    @Test
    void unknownDeviceIs404() throws Exception {
        mvc.perform(get("/api/devices/NOPE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatesConfig() throws Exception {
        String body = """
                {"hostname":"core-r1","ntpServer":"time.google.com","snmpEnabled":false,"loggingLevel":"DEBUG"}
                """;
        mvc.perform(put("/api/devices/R1/config").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.config.hostname").value("core-r1"))
                .andExpect(jsonPath("$.config.loggingLevel").value("DEBUG"));
    }

    @Test
    void rejectsInvalidConfig() throws Exception {
        String body = """
                {"hostname":"","snmpEnabled":true,"loggingLevel":"LOUD"}
                """;
        mvc.perform(put("/api/devices/R1/config").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.loggingLevel").exists());
    }

    @Test
    void restartBringsOfflineDeviceOnline() throws Exception {
        mvc.perform(post("/api/devices/AP1/restart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONLINE"));
    }

    @Test
    void raisesAlertsForDegradedSwitch() throws Exception {
        mvc.perform(get("/api/alerts").param("deviceId", "SW2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.metric=='cpuUsage')]").isNotEmpty());
    }

    @Test
    void returnsAiDiagnosticsForDevice() throws Exception {
        mvc.perform(get("/api/ai/SW2/diagnosis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("SW2"))
                .andExpect(jsonPath("$.predictedIssue").exists())
                .andExpect(jsonPath("$.riskLevel").exists());
    }

    @Test
    void returnsAiExplanationForDevice() throws Exception {
        mvc.perform(get("/api/ai/SW2/explain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("SW2"))
                .andExpect(jsonPath("$.explanation").isString())
                .andExpect(jsonPath("$.signals[0]").exists());
    }
}
