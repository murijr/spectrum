package specs;

import static com.greghaskins.spectrum.PreConditionBlock.with;
import static com.greghaskins.spectrum.PreConditions.Factory.tags;
import static com.greghaskins.spectrum.Spectrum.beforeEach;
import static com.greghaskins.spectrum.Spectrum.configure;
import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import static com.greghaskins.spectrum.Spectrum.let;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.greghaskins.spectrum.Spectrum;
import com.greghaskins.spectrum.SpectrumHelper;
import com.greghaskins.spectrum.SpectrumOptions;

import org.junit.Assert;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.function.Supplier;

@RunWith(Spectrum.class)
public class TaggedSpecs {
  {
    describe("A suite with tagging", () -> {
      beforeEach(TaggedSpecs::clearSystemProperties);

      describe("configured functionally", () -> {

        it("runs completely when no tag selection applied", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsOnly());
          assertThat(result.getIgnoreCount(), is(0));
        });

        it("runs completely when its tag is in the includes list", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsIncluded());
          assertThat(result.getIgnoreCount(), is(0));
        });

        it("does not run when it's missing from the includes", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsNotIncluded());
          assertThat(result.getIgnoreCount(), is(1));
        });

        it("does not run when it's in the excludes list", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsExcluded());
          assertThat(result.getIgnoreCount(), is(1));
        });

        it("is not allowed to be untagged when there's an requiredTags set up", () -> {
          final Result result =
              SpectrumHelper.run(getSuiteWithNoTagsThatShouldNotRunBecauseOfIncludeTags());
          assertThat(result.getIgnoreCount(), is(1));
        });

        it("is possible to exclude individual specs with tags", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithOneExcludedTaggedSpec());
          assertThat(result.getIgnoreCount(), is(1));
        });

        it("only runs specs that match the includeTags filter", () -> {
          final ArrayList<String> specsRun = new ArrayList<>();

          SpectrumHelper.run(() -> {
            configure().includeTags("foo");

            it("should run spec 1", with(tags("foo"), () -> {
              specsRun.add("spec 1");
            }));
            it("should not run spec 2", with(tags("bar"), () -> {
              specsRun.add("spec 2");
            }));
            it("should not run spec 3", () -> {
              specsRun.add("spec 3");
            });

          });

          assertThat(specsRun, hasSize(1));
          assertThat(specsRun, contains("spec 1"));
        });

      });

      describe("with both includes and excludes", () -> {

        Supplier<Result> result = let(() -> SpectrumHelper.run(() -> {

          configure()
              .includeTags("foo", "bar")
              .excludeTags("baz", "qux");

          it("should not run untagged specs", () -> {
            Assert.fail();
          });
          it("should not run unrelated tags", with(tags("blah"), () -> {
            Assert.fail();
          }));

          it("should run spec with at least one tag, foo", with(tags("foo"), () -> {
          }));
          it("should run spec with at least one tag, bar", with(tags("bar"), () -> {
          }));

          it("excludes take precedence over includes, baz", with(tags("foo", "baz"), () -> {
            Assert.fail();
          }));
          it("excludes take precedence over includes, qux", with(tags("bar", "qux"), () -> {
            Assert.fail();
          }));

          it("should run spec with included and unrelated tags", with(tags("foo", "blah"), () -> {
          }));
          it("should not run spec with excluded and unrelated tags",
              with(tags("blah", "qux"), () -> {
                Assert.fail();
              }));

        }));

        it("should not run any specs that match an excluded tag", () -> {
          assertThat(result.get().getFailureCount(), is(0));
        });

        it("should run all specs that match at least one included tag", () -> {
          assertThat(result.get().getRunCount(), is(3));
        });

      });

      describe("configured by annotation", () -> {

        it("runs completely when its tag is in the includes list", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsIncludedByAnnotation());
          assertThat(result.getIgnoreCount(), is(0));
        });

        it("does not run when it's missing from the includes", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsNotIncludedByAnnotation());
          assertThat(result.getIgnoreCount(), is(1));
        });

        it("does not run when it's in the excludes list", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsExcludedByAnnotation());
          assertThat(result.getIgnoreCount(), is(1));
        });

        it("applies tags recursively to child suites", () -> {
          final Result result = SpectrumHelper.run(getExcludedSuiteWithNestedSuite());
          assertThat(result.getIgnoreCount(), is(1));
        });

        it("is possible for the exclusion tags to be modified part way through the definition",
            () -> {
              final Result result = SpectrumHelper.run(getSuiteWhereExclusionIsOverridden());
              assertThat(result.getIgnoreCount(), is(1));
            });

      });

      describe("configured by system property", () -> {

        it("runs completely when its tag is in the includes list", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsIncludedBySystemProperty());
          assertThat(result.getIgnoreCount(), is(0));
        });

        it("does not run when it's missing from the includes", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsNotIncludedBySystemProperty());
          assertThat(result.getIgnoreCount(), is(1));
        });

        it("does not run when it's in the excludes list", () -> {
          final Result result = SpectrumHelper.run(getSuiteWithTagsExcludedBySystemProperty());
          assertThat(result.getIgnoreCount(), is(1));

        });
      });

    });
  }

  private static Class<?> getSuiteWithTagsOnly() {
    class Tagged {
      {
        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that runs", () -> {
            assertTrue(true);
          });
        }));
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithTagsIncluded() {
    class Tagged {
      {
        configure().includeTags("someTag");

        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that runs", () -> {
            assertTrue(true);
          });
        }));
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithTagsNotIncluded() {
    class Tagged {
      {
        // this stops "someTag" from being included by default
        configure().includeTags("someOtherTag");

        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that won't run", () -> {
            assertTrue(true);
          });
        }));
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithTagsExcluded() {
    class Tagged {
      {
        configure().excludeTags("someTag");

        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that won't run", () -> {
            assertTrue(true);
          });
        }));
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithTagsIncludedByAnnotation() {
    @SpectrumOptions(includeTags = "someTag")
    class Tagged {
      {
        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that runs", () -> {
            assertTrue(true);
          });
        }));
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithTagsNotIncludedByAnnotation() {
    @SpectrumOptions(includeTags = "someOtherTag")
    class Tagged {
      {
        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that won't run", () -> {
            assertTrue(true);
          });
        }));
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithNoTagsThatShouldNotRunBecauseOfIncludeTags() {
    class Tagged {
      {
        configure().includeTags("someTag");

        describe("An untagged suite in an 'includeTags' situation", () -> {
          it("has a spec that won't run", () -> {
            assertTrue(true);
          });
        });
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithOneExcludedTaggedSpec() {
    class Tagged {
      {
        configure().excludeTags("exclude me");

        describe("A plain suite", () -> {
          it("has a spec that runs fine", () -> {
            assertTrue(true);
          });

          it("has a spec that will not run", with(tags("exclude me"), () -> {
            assertTrue(true);
          }));
        });
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithTagsExcludedByAnnotation() {
    @SpectrumOptions(excludeTags = "someTag")
    class Tagged {
      {
        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that won't run", () -> {
            assertTrue(true);
          });
        }));
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWithTagsIncludedBySystemProperty() {
    System.setProperty(SpectrumOptions.INCLUDE_TAGS_PROPERTY, "someTag");

    return getSuiteWithTagsOnly();
  }

  private static Class<?> getSuiteWithTagsNotIncludedBySystemProperty() {
    System.setProperty(SpectrumOptions.INCLUDE_TAGS_PROPERTY, "someOtherTag");

    return getSuiteWithTagsOnly();
  }

  private static Class<?> getSuiteWithTagsExcludedBySystemProperty() {
    System.setProperty(SpectrumOptions.EXCLUDE_TAGS_PROPERTY, "someTag");

    return getSuiteWithTagsOnly();
  }

  private static void clearSystemProperties() {
    System.setProperty(SpectrumOptions.INCLUDE_TAGS_PROPERTY, "");
    System.setProperty(SpectrumOptions.EXCLUDE_TAGS_PROPERTY, "");
  }

  private static Class<?> getExcludedSuiteWithNestedSuite() {
    @SpectrumOptions(excludeTags = "someTag")
    class Tagged {
      {
        describe("A suite", () -> {
          describe("With a subsuite", with(tags("someTag"), () -> {
            it("has a spec that's also going to be excluded", () -> {
              assertTrue(true);
            });
          }));
          it("has a spec that will run", () -> {
            assertTrue(true);
          });
        });
      }
    }

    return Tagged.class;
  }

  private static Class<?> getSuiteWhereExclusionIsOverridden() {
    @SpectrumOptions(excludeTags = "someTag")
    class Tagged {
      {
        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that won't run", () -> {
            assertTrue(true);
          });
        }));

        configure().excludeTags("");

        describe("A suite", with(tags("someTag"), () -> {
          it("has a spec that can run this time", () -> {
            assertTrue(true);
          });
        }));
      }
    }

    return Tagged.class;
  }
}
