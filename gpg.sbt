import scala.sys.Prop

useGpg := true

pgpSecretRing := Prop.FileProp("user.home").value / ".gnupg/pubring.gpg"
