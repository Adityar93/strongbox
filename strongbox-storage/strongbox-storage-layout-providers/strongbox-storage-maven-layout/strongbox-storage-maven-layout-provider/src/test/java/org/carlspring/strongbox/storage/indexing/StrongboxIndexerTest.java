package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.indexing.internal.ArtifactEntryRepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.util.IndexContextHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.apache.maven.index.expr.UserInputSearchExpression;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
@Execution(CONCURRENT)
public class StrongboxIndexerTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES_1 = "injector-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "injector-releases-2";

    private static final String REPOSITORY_RELEASES_3 = "injector-releases-3";

    private static final String REPOSITORY_RELEASES_4 = "injector-releases-4";

    private static final String REPOSITORY_RELEASES_5 = "injector-releases-5";

    private static final String REPOSITORY_RELEASES_6 = "injector-releases-6";

    private static final String REPOSITORY_RELEASES_7 = "injector-releases-7";

    private static final String REPOSITORY_RELEASES_8 = "injector-releases-8";

    private static final String REPOSITORY_RELEASES_9 = "injector-releases-9";

    private static final String GROUP_ID = "org.carlspring";

    private static final String ARTIFACT_ID = "properties-injector";

    /**
     * org/carlspring/ioc/PropertyValueInjector
     * org/carlspring/ioc/InjectionException
     * org/carlspring/ioc/PropertyValue
     * org/carlspring/ioc/PropertiesResources
     */
    private Resource jarArtifact = new ClassPathResource("artifacts/properties-injector-1.7.jar");

    /**
     * org/carlspring/ioc/PropertyValueInjector
     * org/carlspring/ioc/InjectionException
     * org/carlspring/ioc/PropertyValue
     * org/carlspring/ioc/PropertiesResources
     */
    private Resource zipArtifact = new ClassPathResource("artifacts/properties-injector-1.7.zip");

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Inject
    private ArtifactEntryRepositoryIndexer artifactEntryRepositoryIndexer;

    @Inject
    private Optional<Indexer> indexer;

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void indexerShouldBeCapableToSearchByClassName(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
            setup = MavenIndexedRepositorySetup.class)
                                                                  Repository repository)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();


        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   jarArtifact.getInputStream());

        String contextId = IndexContextHelper.getContextId(STORAGE0, repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));

        assertOnNewIndexer(repository, q, 1);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void indexerShouldBeCapableToSearchByFQN(@MavenRepository(repositoryId = REPOSITORY_RELEASES_2,
            setup = MavenIndexedRepositorySetup.class)
                                                            Repository repository)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   jarArtifact.getInputStream());

        String contextId = IndexContextHelper.getContextId(STORAGE0,
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                         new UserInputSearchExpression("org.carlspring.ioc.PropertyValueInjector"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));

        assertOnNewIndexer(repository, q, 1);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void indexerShouldBeCapableToSearchByFullSha1Hash(@MavenRepository(repositoryId = REPOSITORY_RELEASES_3,
            setup = MavenIndexedRepositorySetup.class)
                                                                     Repository repository,
                                                             @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_3,
                                                                     id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                     versions = { "1.8" })
                                                                     Path artifactPath)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.8/properties-injector-1.8.jar.sha1");

        String sha1 = Files.readAllLines(repositoryPath).get(0);

        String contextId = IndexContextHelper.getContextId(repository.getStorage().getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.SHA1, new SourcedSearchExpression(sha1));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));

        assertOnNewIndexer(repository, q, 1);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void indexerShouldBeCapableToSearchByPartialSha1Hash(@MavenRepository(repositoryId = REPOSITORY_RELEASES_4,
            setup = MavenIndexedRepositorySetup.class)
                                                                        Repository repository,
                                                                @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_4,
                                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                        classifiers = { "javadoc",
                                                                                        "sources" },
                                                                        versions = { "1.8" })
                                                                        Path artifactPath)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.8/properties-injector-1.8.jar.sha1");

        String sha1 = Files.readAllLines(repositoryPath).get(0);

        String contextId = IndexContextHelper.getContextId(repository.getStorage().getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.SHA1, new UserInputSearchExpression(sha1.substring(0, 8)));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));

        assertOnNewIndexer(repository, q, 1);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void indexerShouldBeCapableToSearchByClassNameFromZippedArtifact(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_5,
                    setup = MavenIndexedRepositorySetup.class)
                    Repository repository)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());

        String contextId = IndexContextHelper.getContextId(repository.getStorage().getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));

        assertOnNewIndexer(repository, q, 1);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void indexerShouldBeCapableToSearchByFQNFromZippedArtifact(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_6,
                    setup = MavenIndexedRepositorySetup.class)
                    Repository repository)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());

        String contextId = IndexContextHelper.getContextId(repository.getStorage().getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                         new UserInputSearchExpression("org.carlspring.ioc.PropertyValueInjector"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));

        assertOnNewIndexer(repository, q, 1);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByJavadoc(@MavenRepository(repositoryId = REPOSITORY_RELEASES_7,
            setup = MavenIndexedRepositorySetup.class)
                                                         Repository repository,
                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_7,
                                                         id = GROUP_ID + ":" + ARTIFACT_ID,
                                                         classifiers = { "javadoc",
                                                                         "sources" },
                                                         versions = { "1.8" })
                                                         Path artifactPath)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        String contextId = IndexContextHelper.getContextId(repository.getStorage().getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);

        final Query groupIdQ =
                indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.carlspring"));
        final Query artifactIdQ =
                indexer.constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression("properties-injector"));

        final BooleanQuery q = new BooleanQuery.Builder()
                                       .add(groupIdQ, BooleanClause.Occur.MUST)
                                       .add(artifactIdQ, BooleanClause.Occur.MUST)
                                       .add(indexer.constructQuery(MAVEN.EXTENSION, new SourcedSearchExpression("jar")),
                                            BooleanClause.Occur.MUST)
                                       .add(indexer.constructQuery(MAVEN.CLASSIFIER,
                                                                   new SourcedSearchExpression("javadoc")),
                                            BooleanClause.Occur.MUST)
                                       .build();

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));

        assertOnNewIndexer(repository, q, 1);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchAllJarsWithClassifiers(@MavenRepository(repositoryId = REPOSITORY_RELEASES_8,
            setup = MavenIndexedRepositorySetup.class)
                                                                      Repository repository,
                                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_8,
                                                                      id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                      classifiers = { "javadoc",
                                                                                      "sources" },
                                                                      versions = { "1.8" })
                                                                      Path artifactPath)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        String contextId = IndexContextHelper.getContextId(repository.getStorage().getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);

        final Query groupIdQ =
                indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.carlspring"));
        final Query artifactIdQ =
                indexer.constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression("properties-injector"));

        final BooleanQuery q = new BooleanQuery.Builder()
                                       .add(groupIdQ, BooleanClause.Occur.MUST)
                                       .add(artifactIdQ, BooleanClause.Occur.MUST)
                                       .add(indexer.constructQuery(MAVEN.EXTENSION, new SourcedSearchExpression("jar")),
                                            BooleanClause.Occur.MUST)
                                       .build();

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(3));

        assertOnNewIndexer(repository, q, 3);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByName(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_9, setup = MavenIndexedRepositorySetup.class)
                    Repository repository,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_9,
                    id = GROUP_ID + ":" + ARTIFACT_ID,
                    classifiers = { "javadoc",
                                    "sources" },
                    versions = { "1.8" })
                    Path artifactPath)
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        String contextId = IndexContextHelper.getContextId(repository.getStorage().getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);

        final Query q =
                indexer.constructQuery(MAVEN.NAME, new SourcedSearchExpression("org.carlspring"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(3));

        assertOnNewIndexer(repository, q, 3);
    }

    private void assertOnNewIndexer(Repository repository,
                                    Query q,
                                    int expectedHitsCount)
            throws IOException
    {
        IndexingContext artifactEntryIndexingContext = artifactEntryRepositoryIndexer.generateIndexingContext(
                repository.getStorage().getId(),
                repository.getId());
        FlatSearchResponse response = indexer.get().searchFlat(new FlatSearchRequest(q, artifactEntryIndexingContext));
        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(expectedHitsCount));
    }

}
