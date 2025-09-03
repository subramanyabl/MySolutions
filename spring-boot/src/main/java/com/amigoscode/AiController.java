package com.amigoscode;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("api/v1/ai")
@RestController
public class AiController {

    private final ChatClient chatClient;
    private final OpenAiAudioApi openAiAudioApi;

    public AiController(ChatClient chatClient,
                        OpenAiAudioApi openAiAudioApi) {
        this.chatClient = chatClient;
        this.openAiAudioApi = openAiAudioApi;
    }

    @GetMapping("chat")
    public List<JavaFrameworkRank> chat() {
        var output = chatClient.prompt()
                .user("what are the top 5 best java frameworks to build web apps. I want the output to be minimal and in a list format.")
                .call()
                .entity(new ParameterizedTypeReference<List<JavaFrameworkRank>>() {
                });
        return output;
    }

    @GetMapping("images/describe")
    public String describeImage() throws IOException {
        ClassPathResource pancake  = new ClassPathResource("pancake.jpg");
        ClassPathResource thumbnail  = new ClassPathResource("thumbnail.png");
        String output = chatClient.prompt()
                .messages(new UserMessage(
                        "Describe this image",
                        List.of(
                                new Media(MimeTypeUtils.IMAGE_JPEG, pancake),
                                new Media(MimeTypeUtils.IMAGE_PNG, thumbnail)
                        )
                ))
                .call()
                .content();
        return output;
    }

    @GetMapping("openai/transcribe")
    public String transcribeAudio() {
        var audio  = new ClassPathResource("test-audio.m4a");
        var model = new OpenAiAudioTranscriptionModel(openAiAudioApi);
        var options = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f)
                .withLanguage("en")
                .build();
        var prompt = new AudioTranscriptionPrompt(audio, options);
        var result = model.call(prompt).getResult().getOutput();
        return result;
    }

}
