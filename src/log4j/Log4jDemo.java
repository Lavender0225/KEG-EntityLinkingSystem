package log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 /**
  * Using log demo
  * @author Jingzhang
  *
  */
public class Log4jDemo {
    private static final Logger logger = LogManager.getLogger("Log4jDemo");
    public static void main(String[] args) {
        logger.info("Hello, World!");
    }
}