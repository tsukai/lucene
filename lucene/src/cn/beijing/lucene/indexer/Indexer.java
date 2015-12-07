/**
 * 
 */
package cn.beijing.lucene.indexer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * IndexWriter(写索引)是索引过程的核心组件。
 * 这个类负责创建新索引或者打开已有索引，以及向索引中添加、删除或者更新被索引文档的信息。（不能用户读取或搜索索引）
 * IndexWriter需要开辟一定空间来存储索引，该功能可以由Directory来完成。
 * 
 * Directory描述了Lucene索引的存放位置
 * 他的子类负责具体指定索引的存储路径。
 * 
 * Analyzer 文件在被索引之前需要经过Analyzer分析器处理。
 * 负责从被索引文本文件中提取语汇单元，并提出剩下的无用信息。如果被索引内容不是纯文本文件，需要先将其转换为文本文档。
 * 分析器的分析对象为文档，该文档包含一些分离的能被索引的域.
 * 
 * Document文档代表一些域（Field）的集合。
 * 可将其理解为虚拟文档——比如web页面、E-mail信息或者文本文件，可以从中取回大量数据。
 * 文档的域代表文档或者和文档相关的一些元数据。文档的数据源对于Lucene来说无关紧要。
 * Lucene只处理从二进制文档中提取的以Field实例形式出现的文本。
 * 
 * Lucene只处理文本和数字。Lucene的内核本身只处理java.lang.String、java.io.Reader对象和本地数字类型。
 * 
 * Field是指包含能被索引的文本内容的类。
 * 每个域都有一个域名和对应的阈值，以及一组选项来精确控制Lucene索引操作各个域值。文档可能拥有不止一个同名的域。在这种情况下，域的值就按照索引操作顺序添加进去。
 * 在搜索时，所有域的文本就好像连在一起，作为一个文本域来处理。
 * @author zukai 2015-12-07
 */
public class Indexer {
	private IndexWriter writer;
	public Indexer(String indexDir) throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		//创建Lucene Index Writer
		writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30), true,IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	public void close() throws IOException{
		writer.close();
	}
	
	public int index(String dataDir,FileFilter filter) throws Exception{
		File[] files = new File(dataDir).listFiles();
		for(File f : files){
			if(!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() && (filter == null || filter.accept(f))){
				indexFile(f);
			}
		}
		//返回被索引文档数
		return writer.numDocs();
	}

	private void indexFile(File f) throws Exception{
		System.out.println("Index "+f.getCanonicalPath());
		Document doc = getDocument(f);
		writer.addDocument(doc);//向lucene索引中添加文档
	}

	private Document getDocument(File f) throws Exception{
		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));//索引文件内容
		doc.add(new Field("filename", f.getName(),Field.Store.YES,Field.Index.NOT_ANALYZED));//索引文件名
		doc.add(new Field("fullpath", f.getCanonicalPath(),Field.Store.YES,Field.Index.NOT_ANALYZED));//索引文件完整路径
		return doc;
	}
	
	private static class TextFilesFilter implements FileFilter{

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().toLowerCase().endsWith(".txt");//只索引*.txt文件
		}
		
	}

	public static void main(String[] args) throws Exception{
		if(args.length != 2){
			throw new IllegalArgumentException("Usage: java "+Indexer.class.getName()+" <index dir> <data dir>.");
		}
		String indexDir = args[0];//在指定目录创建索引
		String dataDir = args[1];//对指定目录中的*.txt文件进行多用
		long start = System.currentTimeMillis();
		Indexer indexer = new Indexer(indexDir);
		int numIndexed ;
		try{
			numIndexed = indexer.index(dataDir, new TextFilesFilter());
		}finally{
			indexer.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Indexing "+numIndexed+" files took "+(end-start)+" milliseconds");
	}
}
