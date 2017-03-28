package aether
package internal

import org.eclipse.aether.{RepositoryEvent, AbstractRepositoryListener}
import sbt.Logger

class ConsoleRepositoryListener(logger: Logger) extends AbstractRepositoryListener {
  override def artifactDeployed(event: RepositoryEvent) {
    logger.info("Deployed " + event.getArtifact + " to " + event.getRepository)
  }

  override def artifactDeploying(event: RepositoryEvent) {
    logger.info("Deploying " + event.getArtifact + " to " + event.getRepository)
  }

  override def artifactDescriptorInvalid(event: RepositoryEvent) {
    logger.info("Invalid artifact descriptor for " + event.getArtifact + ": "
      + event.getException.getMessage)
  }

  override def artifactDescriptorMissing(event: RepositoryEvent) {
    logger.info("Missing artifact descriptor for " + event.getArtifact)
  }

  override def artifactDownloaded(event: RepositoryEvent) {
    logger.info("Downloaded artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactDownloading(event: RepositoryEvent) {
    logger.info("Downloading artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactInstalled(event: RepositoryEvent) {
    logger.info("Installed " + event.getArtifact + " to " + event.getFile)
  }

  override def artifactInstalling(event: RepositoryEvent) {
    logger.info("Installing " + event.getArtifact + " to " + event.getFile)
  }

  override def artifactResolved(event: RepositoryEvent) {
    logger.info("Resolved artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactResolving(event: RepositoryEvent) {
    logger.info("Resolving artifact " + event.getArtifact)
  }

  override def metadataDeployed(event: RepositoryEvent) {
    logger.info("Deployed " + event.getMetadata + " to " + event.getRepository)
  }

  override def metadataDeploying(event: RepositoryEvent) {
    logger.info("Deploying " + event.getMetadata + " to " + event.getRepository)
  }

  override def metadataDownloaded(event: RepositoryEvent) {
    logger.info("Downloaded metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataDownloading(event: RepositoryEvent) {
    logger.info("Downloading metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataInstalled(event: RepositoryEvent) {
    logger.info("Installed " + event.getMetadata + " to " + event.getFile)
  }

  override def metadataInstalling(event: RepositoryEvent) {
    logger.info("Installing " + event.getMetadata + " to " + event.getFile)
  }

  override def metadataInvalid(event: RepositoryEvent) {
    logger.info("Invalid metadata " + event.getMetadata)
  }

  override def metadataResolved(event: RepositoryEvent) {
    logger.info("Resolved metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataResolving(event: RepositoryEvent) {
    logger.info("Resolving metadata " + event.getMetadata + " from " + event.getRepository)
  }
}
