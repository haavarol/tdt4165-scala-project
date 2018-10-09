import scala.concurrent.ExecutionContext
import scala.concurrent.forkjoin.ForkJoinPool

class Bank(val allowedAttempts: Integer = 3) {

    //private val uid = 0 TODO: find out if we need this and why
    private val transactionsQueue: TransactionQueue = new TransactionQueue()
    private val processedTransactions: TransactionQueue = new TransactionQueue()
    private val executorContext = ExecutionContext.fromExecutor(new ForkJoinPool(100)) // TODO: Find out if this is the type of EC we need

    private var account_id_counter = 0 // previous unique account ID

    def addTransactionToQueue(from: Account, to: Account, amount: Double): Unit = {
      val t = transactionsQueue push new Transaction(
        transactionsQueue, processedTransactions, from, to, amount, allowedAttempts)

        // TODO: submit thread to executor context
    }

    def generateAccountId: Int = this.synchronized {
        account_id_counter += 1
        return account_id_counter
    }

    private def processTransactions: Unit = {
        // TODO
    }

    def addAccount(initialBalance: Double): Account = {
        new Account(this, initialBalance)
    }

    def getProcessedTransactionsAsList: List[Transaction] = {
        processedTransactions.iterator.toList
    }

}
