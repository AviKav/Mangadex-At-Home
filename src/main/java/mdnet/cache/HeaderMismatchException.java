package mdnet.cache;

import java.io.IOException;
import java.util.Arrays;

public class HeaderMismatchException extends IOException {
	public HeaderMismatchException(String[] actual, String[] expected) {
		super("expected header " + Arrays.toString(expected) + ", found " + Arrays.toString(actual));
	}
}
