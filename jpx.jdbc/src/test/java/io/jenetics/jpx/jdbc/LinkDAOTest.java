/*
 * Java GPX Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.jpx.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import io.jenetics.jpx.Link;
import io.jenetics.jpx.LinkTest;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class LinkDAOTest {

	private final DB db = H2DB.newTestInstance();

	private final List<Link> links = nextLinks(new Random(123), 20);

	private static List<Link> nextLinks(final Random random, final int count) {
		final List<Link> links = new ArrayList<>();
		for (int i = 0; i < count; ++i) {
			links.add(LinkTest.nextLink(random));
		}

		return links;
	}

	@BeforeSuite
	public void setup() throws IOException, SQLException {
		final String[] queries = IO.
			toSQLText(getClass().getResourceAsStream("/model-mysql.sql"))
			.split(";");

		db.transaction(conn -> {
			for (String query : queries) {
				try (Statement stmt = conn.createStatement()) {
					stmt.execute(query);
				}
			}
		});
	}

	@AfterSuite
	public void shutdown() throws SQLException {
		db.close();
	}

	@Test
	public void insert() throws SQLException {
		db.transaction(conn -> {
			new LinkDAO(conn).insert(links);
		});
	}

	@Test(dependsOnMethods = "insert")
	public void select() throws SQLException {
		final List<Stored<Link>> existing = db.transaction(conn -> {
			return new LinkDAO(conn).select();
		});

		Assert.assertEquals(
			existing.stream()
				.map(Stored::value)
				.collect(Collectors.toSet()),
			links.stream()
				.collect(Collectors.toSet())
		);
	}

	@Test(dependsOnMethods = "select")
	public void update() throws SQLException {
		final List<Stored<Link>> existing = db.transaction(conn -> {
			return new LinkDAO(conn).select();
		});

		db.transaction(conn -> {
			final Stored<Link> updated = existing.get(0)
				.map(l -> Link.of(l.getHref(), "Other text", null));

			Assert.assertEquals(
				new LinkDAO(conn).update(updated),
				updated
			);
		});
	}

	@Test(dependsOnMethods = "update")
	public void put() throws SQLException {
		db.transaction(conn -> {
			final LinkDAO dao = new LinkDAO(conn);

			dao.put(links);

			Assert.assertEquals(
				dao.select().stream()
					.map(Stored::value)
					.collect(Collectors.toSet()),
				links.stream()
					.collect(Collectors.toSet())
			);
		});
	}

}
