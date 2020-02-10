import scala.sys.Prop

pgpSecretRing := Prop.FileProp("user.home").value / ".gnupg/pubring.gpg"
