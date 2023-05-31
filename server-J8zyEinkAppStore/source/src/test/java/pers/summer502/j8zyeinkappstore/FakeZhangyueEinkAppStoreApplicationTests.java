package pers.summer502.j8zyeinkappstore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class FakeZhangyueEinkAppStoreApplicationTests {

	@Test
	void contextLoads() {
		JacksonJsonParser jacksonJsonParser = new JacksonJsonParser();
		List<Object> objects = jacksonJsonParser.parseList("[{\"name\":1,\"address\":\"qqqa\"}," +
				"{\"name\":5,\"address\":\"hhqqqa\"}]");
		System.out.println(objects);
	}

}
