package com.nwdigital.task.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Media;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.core.Local;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nwdigital.task.backend.models.Block;
import com.nwdigital.task.backend.models.ChatBotFlow;
import com.nwdigital.task.backend.models.ConversationHistory;
import com.nwdigital.task.backend.repository.ChatBotRepository;
import com.nwdigital.task.backend.repository.ConversationHistoryRepository;
import com.nwdigital.task.backend.services.ChatBotService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatBotServiceTest {

    @Autowired
    private ChatBotRepository chatBotRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationHistoryRepository conversationHistoryRepository;

    
    @Value("${OPENAI_SECRETKEY:}")
    private String OPENAI_SECRETKEY;

    @Autowired
    private ChatBotService chatBotService;
   

    @BeforeEach
    void setupChatService() throws Exception {
        chatBotService = new ChatBotService(chatBotRepository);
        // Set the conversation history repository
        Field f = ChatBotService.class.getDeclaredField("conversationHistoryRepository");
        f.setAccessible(true);
        f.set(chatBotService, conversationHistoryRepository);
        
        // Set the OpenAI secret key
        Field openaiField = ChatBotService.class.getDeclaredField("OPENAI_SECRETKEY");
        openaiField.setAccessible(true);
        openaiField.set(chatBotService, OPENAI_SECRETKEY);
    }

    @Test
    void setup() throws Exception{
        Block startBlock = new Block();
        startBlock.setId("start");
        startBlock.setType("send_message");
        startBlock.setMessage("Hello there!");
        startBlock.setNextBlock("wait_response1");

        Block responseblock = new Block();
        responseblock.setId("wait_response1");
        responseblock.setType("wait_response");
        responseblock.setMessage("");
        responseblock.setNextBlock("intent_block");

        Block intentBlock = new Block();
        intentBlock.setId("intent_block");
        intentBlock.setType("recognize_intent");
        intentBlock.setIntents(Arrays.asList("time", "weather"));
        intentBlock.setBranches(Map.of("time", "time_response", "weather", "weather_response"));

        Block timeBlock = new Block();
        timeBlock.setId("time_response");
        timeBlock.setType("send_message");
        timeBlock.setMessage("Time now is: 00:00");

        Block weatherBlock = new Block();
        weatherBlock.setId("weather_response");
        weatherBlock.setType("send_message");
        weatherBlock.setMessage("sunny 10 degrees");

        Block endBlock = new Block();
        endBlock.setId("end");
        endBlock.setType("end");
        endBlock.setMessage("bye");

        ChatBotFlow mockFlow = new ChatBotFlow();
        mockFlow.setStart_block_id("start");
        mockFlow.setBlocks(Arrays.asList(startBlock, responseblock, intentBlock, timeBlock, weatherBlock, endBlock));

        mockMvc.perform(post("/api/v1/createConfig")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(mockFlow)))
            .andExpect(status().isOk());

        chatBotRepository.deleteAll();
        chatBotRepository.save(mockFlow);
    
        Field f = ChatBotService.class.getDeclaredField("conversationHistoryRepository");
        f.setAccessible(true);
        f.set(chatBotService, conversationHistoryRepository);
        String response = chatBotService.processMessage("test-user", "");
        assertTrue(response.contains("Hello there!"));
        response = chatBotService.processMessage("test-user", "What is the weather?");
        assertTrue(response.contains("sunny 10 degrees"));
        response = chatBotService.processMessage("test-user", "what time is it?");
        // LocalDateTime now = LocalDateTime.now();
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        // System.out.println(response);
        // assertTrue(response.contains("Time is: " + now.format(formatter)));
        response = chatBotService.processMessage("test-user", "");
    }

    @Test
    void testGetMethod() throws Exception {
        mockMvc.perform(get("/api/v1/getConfig")
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(chatBotRepository)))
        .andExpect(status().isOk());
    }

    @Test
    void testHandleSendMessage() throws Exception {
        Block block = new Block();
        block.setId("start");
        block.setType("send_message");
        block.setMessage("hi");
        block.setNextBlock("wait");

        ChatBotFlow flow = new ChatBotFlow();
        flow.setBlocks(List.of(block));

        Method method = ChatBotService.class.getDeclaredMethod("handleSendMessage",String.class, Block.class, ChatBotFlow.class, String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(chatBotService, "test-user", block, flow, "");

        assertTrue(result.contains("hi"));
    }

    @Test
    void testGetDynamicMessage() throws Exception {
        Block block = new Block();
        block.setId("time_response");
        block.setType("send_message");

        ChatBotFlow flow = new ChatBotFlow();
        flow.setBlocks(List.of(block));
        Method method = ChatBotService.class.getDeclaredMethod("getDynamicMessage", Block.class);
        method.setAccessible(true);
        String result = (String) method.invoke(chatBotService, block);
        block.setId("weather_response");
        block.setMessage("studeno");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm:ss");
        assertTrue(result.contains("Time is: " + now.format(formatter)));
        result = (String) method.invoke(chatBotService, block);
        assertTrue(result.contains("studeno"));
    }
}