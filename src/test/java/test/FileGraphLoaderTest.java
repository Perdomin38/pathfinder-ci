package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.ulpgc.pathfinder.FileGraphLoader;
import software.ulpgc.pathfinder.GraphContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileGraphLoaderTest {
	@TempDir
	Path tmp;

	private GraphContainer load(String... lines) throws IOException {
		Path file = tmp.resolve("graph.csv");
		Files.write(file, List.of(lines));
		return new FileGraphLoader(file.toFile()).load();
	}

	@Test
	void loadsWeightedEdgesFromFile() throws IOException {
		GraphContainer graph = load("A,B,1.5", "B,C,2", "A,C,10");
		assertEquals(List.of("A", "B", "C"), graph.shortestPathBetween("A", "C"));
		assertEquals(3.5, graph.pathWeightBetween("A", "C"), 1e-9);
	}

	@Test
	void edgesAreUndirected() throws IOException {
		GraphContainer graph = load("A,B,4");
		assertEquals(List.of("B", "A"), graph.shortestPathBetween("B", "A"));
		assertEquals(4.0, graph.pathWeightBetween("B", "A"), 1e-9);
	}

	@Test
	void skipsLinesWithWrongNumberOfFields() throws IOException {
		GraphContainer graph = load("A,B,1", "B,C", "C,D,1,extra", "B,E,2");
		assertEquals(3.0, graph.pathWeightBetween("A", "E"), 1e-9);
		assertThrows(IllegalArgumentException.class, () -> graph.shortestPathBetween("A", "C"));
		assertThrows(IllegalArgumentException.class, () -> graph.shortestPathBetween("A", "D"));
	}

	@Test
	void skipsLinesWithNonNumericWeight() throws IOException {
		GraphContainer graph = load("A,B,uno", "A,C,1");
		assertEquals(1.0, graph.pathWeightBetween("A", "C"), 1e-9);
		assertThrows(IllegalArgumentException.class, () -> graph.pathWeightBetween("A", "B"));
	}

	@Test
	void emptyFileProducesEmptyGraph() throws IOException {
		GraphContainer graph = load();
		assertThrows(IllegalArgumentException.class, () -> graph.shortestPathBetween("A", "B"));
	}

	@Test
	void missingFileThrowsIOException() {
		File missing = tmp.resolve("no-existe.csv").toFile();
		assertThrows(IOException.class, () -> new FileGraphLoader(missing).load());
	}
}
