using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

internal static class StartSatisfactory
{
    [STAThread]
    private static void Main()
    {
        string exeDir = AppDomain.CurrentDomain.BaseDirectory;
        string envExample = Path.Combine(exeDir, ".env.example");
        string env = Path.Combine(exeDir, ".env");
        string sharedDir = Path.Combine(exeDir, "shared");

        try
        {
            if (!CommandSucceeds("docker", "--version", exeDir))
            {
                MessageBox.Show("未检测到 Docker。请先安装并启动 Docker Desktop。", "启动失败", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            if (!File.Exists(env) && File.Exists(envExample))
            {
                File.Copy(envExample, env);
            }

            if (!Directory.Exists(sharedDir))
            {
                Directory.CreateDirectory(sharedDir);
            }

            RunCommandOrThrow("docker", "compose up -d --build", exeDir);

            Process.Start(new ProcessStartInfo
            {
                FileName = "http://localhost:4200",
                UseShellExecute = true
            });

            MessageBox.Show("启动完成。\n前端: http://localhost:4200\n后端: http://localhost:8080/swagger-ui.html", "Satisfactory Project", MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
        catch (Exception ex)
        {
            MessageBox.Show("启动失败:\n" + ex.Message, "Satisfactory Project", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }

    private static bool CommandSucceeds(string fileName, string arguments, string workingDirectory)
    {
        try
        {
            using (Process process = new Process
            {
                StartInfo = new ProcessStartInfo
                {
                    FileName = fileName,
                    Arguments = arguments,
                    WorkingDirectory = workingDirectory,
                    CreateNoWindow = true,
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true
                }
            })
            {
                process.Start();
                process.WaitForExit();
                return process.ExitCode == 0;
            }
        }
        catch
        {
            return false;
        }
    }

    private static void RunCommandOrThrow(string fileName, string arguments, string workingDirectory)
    {
        using (Process process = new Process
        {
            StartInfo = new ProcessStartInfo
            {
                FileName = fileName,
                Arguments = arguments,
                WorkingDirectory = workingDirectory,
                CreateNoWindow = true,
                UseShellExecute = false,
                RedirectStandardOutput = true,
                RedirectStandardError = true
            }
        })
        {
            process.Start();
            string stdout = process.StandardOutput.ReadToEnd();
            string stderr = process.StandardError.ReadToEnd();
            process.WaitForExit();

            if (process.ExitCode != 0)
            {
                throw new Exception((stdout + Environment.NewLine + stderr).Trim());
            }
        }
    }
}
