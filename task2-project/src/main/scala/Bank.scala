import java.util.NoSuchElementException
import scala.collection.mutable
import akka.actor._
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import akka.util.Timeout

case class GetAccountRequest(accountId: String)

case class CreateAccountRequest(initialBalance: Double)

case class IdentifyActor()

class Bank(val bankId: String) extends Actor {

    val accountCounter = new AtomicInteger(1000)

    def createAccount(initialBalance: Double): ActorRef = {
        val i = accountCounter.incrementAndGet()
        val id = s"$i"
        val account = BankManager.createAccount(id, this.bankId, initialBalance)
        println(account)
        account
    }

    def findAccount(accountId: String): Option[ActorRef] = {
        try {
            Some(BankManager.findAccount(this.bankId, accountId))
        } catch {
            case e: NumberFormatException => None
        }
    }

    def findAccount1(accountId: String): ActorRef = {
        println("TEST1", s"$accountId")
        BankManager.findAccount(this.bankId, accountId)
    }

    def findOtherBank(bankId: String): Option[ActorRef] = {
        // Use BankManager to look up a different bank with ID bankId
        try {
            Some(BankManager.findBank(bankId))
        } catch {
            case e: NumberFormatException => None
        }
    }

    override def receive = {
        case CreateAccountRequest(initialBalance) => 
            sender ! createAccount(initialBalance) // Create a new account
        case GetAccountRequest(id) => 
            println("TEST")
            findAccount1(id) // Return account
        case IdentifyActor => sender ! this
        case t: Transaction => {
            println("Banken får t")
            processTransaction(t)
        }

        case t: TransactionRequestReceipt => {
        // Forward receipt
        ???
        }

        case msg => println(s"$msg")
    }

    def processTransaction(t: Transaction): Unit = {
        implicit val timeout = new Timeout(5 seconds)
        val isInternal = t.to.length <= 4
        val toBankId = if (isInternal) bankId else t.to.substring(0, 4)
        val toAccountId = if (isInternal) t.to else t.to.substring(4)
        val transactionStatus = t.status
        println("Hei!")
        // This method should forward Transaction t to an account or another bank, depending on the "to"-address.
        // HINT: Make use of the variables that have been defined above.
        if(isInternal || toBankId == bankId){
            findAccount1(toAccountId) ! t
        }
        else {
        }
    }
}