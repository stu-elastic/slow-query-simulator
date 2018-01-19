/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.slowquerysimulator;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.Set;

public class SlowQuerySimulatorBuilder extends AbstractQueryBuilder<SlowQuerySimulatorBuilder> {
    /*
     {
        "query": {
          "slow": { <--- NAME
            "sleepSec": 5
          }
        }
      }
     */
    // The name of the query
    public static final String NAME = "slow";
    private int sleepSec;

    public void setSleepSec(int sleepSec) { this.sleepSec = sleepSec; }
    /**
     * Read from a stream.
     */
    public SlowQuerySimulatorBuilder() {}

    public SlowQuerySimulatorBuilder(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    protected void doWriteTo(StreamOutput out) {}

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.endObject();
    }

    private static final ObjectParser<SlowQuerySimulatorBuilder, Void> PARSER = new ObjectParser<>(NAME, SlowQuerySimulatorBuilder::new);

    public static SlowQuerySimulatorBuilder fromXContent(XContentParser parser) {
        try {
            return PARSER.apply(parser, null);
        } catch (IllegalArgumentException e) {
            throw new ParsingException(parser.getTokenLocation(), e.getMessage(), e);
        }
    }

    // Parse the sleepSec field from the "slow" object as an Int
    static {
        PARSER.declareInt(SlowQuerySimulatorBuilder::setSleepSec, new ParseField("sleepSec"));
    }

    @Override
    protected Query doToQuery(QueryShardContext context) {
        return new Query() {
            @Override
            public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
                return new Weight(this) {
                    @Override
                    public void extractTerms(Set<Term> terms) {
                    }

                    @Override
                    public Explanation explain(LeafReaderContext context, int doc) throws IOException {
                        return Explanation.noMatch("sleep=" + sleepSec);
                    }

                    @Override
                    public Scorer scorer(LeafReaderContext context) throws IOException {
                        // NOTE(stu): This is where we sleep
                        try {
                            Thread.sleep(sleepSec * 1000);
                        } catch (InterruptedException err) {
                            throw new RuntimeException(err);
                        }
                        return null;
                    }
                };
            }

            @Override
            public String toString(String field) {
                return null;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        };
    }

    @Override
    protected boolean doEquals(SlowQuerySimulatorBuilder other) {
        return true;
    }

    @Override
    protected int doHashCode() {
        return 0;
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

}
